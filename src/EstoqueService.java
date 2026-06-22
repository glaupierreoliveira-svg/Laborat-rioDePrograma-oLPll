import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe responsável por toda a lógica de negócio do sistema:
 * cadastro, controle de estoque (entradas/saídas), consultas,
 * atualizações, remoção (lógica) e relatórios.
 *
 * Centraliza as regras descritas na especificação do sistema.
 */
public class EstoqueService {

    // Map garante código único (chave) e mantém ordem de inserção
    private final Map<String, Produto> produtos = new LinkedHashMap<>();
    private final List<Movimentacao> movimentacoes = new ArrayList<>();

    // ===================== 1. CADASTRO DE PRODUTOS =====================

    /**
     * Cadastra um novo produto no sistema.
     * Regras aplicadas:
     * - Código, nome, categoria, quantidade e preço são obrigatórios.
     * - Código deve ser único.
     * - Quantidade e preço não podem ser negativos.
     */
    public Produto cadastrarProduto(String codigo, String nome, String categoria,
                                     int quantidade, double preco, int estoqueMinimo) throws EstoqueException {

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new EstoqueException("O código do produto é obrigatório.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new EstoqueException("O nome do produto é obrigatório.");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new EstoqueException("A categoria do produto é obrigatória.");
        }
        if (quantidade < 0) {
            throw new EstoqueException("A quantidade não pode ser negativa.");
        }
        if (preco < 0) {
            throw new EstoqueException("O preço não pode ser negativo.");
        }
        if (produtos.containsKey(codigo)) {
            throw new EstoqueException("Já existe um produto cadastrado com o código '" + codigo + "'.");
        }
        if (estoqueMinimo < 0) {
            throw new EstoqueException("O estoque mínimo não pode ser negativo.");
        }

        Produto produto = new Produto(codigo, nome, categoria, quantidade, preco, estoqueMinimo);
        produtos.put(codigo, produto);
        return produto;
    }

    // ===================== 2. CONTROLE DE ESTOQUE =====================

    /**
     * Registra entrada de mercadoria, atualizando a quantidade em estoque.
     * Regra: somente produtos previamente cadastrados e ativos podem receber entradas.
     */
    public void registrarEntrada(String codigo, int quantidade, String responsavel) throws EstoqueException {
        Produto produto = buscarPorCodigoObrigatorio(codigo);

        if (!produto.isAtivo()) {
            throw new EstoqueException("Produto inativo não pode ser movimentado.");
        }
        if (quantidade <= 0) {
            throw new EstoqueException("A quantidade de entrada deve ser maior que zero.");
        }
        if (responsavel == null || responsavel.trim().isEmpty()) {
            throw new EstoqueException("O responsável pela movimentação é obrigatório.");
        }

        produto.adicionarEstoque(quantidade);
        movimentacoes.add(new Movimentacao(codigo, Movimentacao.Tipo.ENTRADA, quantidade, responsavel));
    }

    /**
     * Registra saída de mercadoria, reduzindo a quantidade em estoque.
     * Regra: não permite saída maior que o saldo disponível.
     */
    public void registrarSaida(String codigo, int quantidade, String responsavel) throws EstoqueException {
        Produto produto = buscarPorCodigoObrigatorio(codigo);

        if (!produto.isAtivo()) {
            throw new EstoqueException("Produto inativo não pode ser movimentado.");
        }
        if (quantidade <= 0) {
            throw new EstoqueException("A quantidade de saída deve ser maior que zero.");
        }
        if (quantidade > produto.getQuantidade()) {
            throw new EstoqueException(
                    "Saída não permitida: quantidade solicitada (" + quantidade +
                            ") é maior que o saldo disponível (" + produto.getQuantidade() + ")."
            );
        }
        if (responsavel == null || responsavel.trim().isEmpty()) {
            throw new EstoqueException("O responsável pela movimentação é obrigatório.");
        }

        produto.removerEstoque(quantidade);
        movimentacoes.add(new Movimentacao(codigo, Movimentacao.Tipo.SAIDA, quantidade, responsavel));
    }

    /**
     * Retorna a lista de produtos cujo estoque está em nível baixo
     * (quantidade <= estoque mínimo definido), sinalizando necessidade de reposição.
     */
    public List<Produto> listarEstoqueBaixo() {
        List<Produto> resultado = new ArrayList<>();
        for (Produto p : produtos.values()) {
            if (p.isAtivo() && p.isEstoqueBaixo()) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // ===================== 3. CONSULTA DE PRODUTOS =====================

    public Produto consultarPorCodigo(String codigo) {
        return produtos.get(codigo);
    }

    public List<Produto> consultarPorNome(String nome) {
        List<Produto> resultado = new ArrayList<>();
        String busca = nome.toLowerCase();
        for (Produto p : produtos.values()) {
            if (p.getNome().toLowerCase().contains(busca)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public List<Produto> consultarPorCategoria(String categoria) {
        List<Produto> resultado = new ArrayList<>();
        String busca = categoria.toLowerCase();
        for (Produto p : produtos.values()) {
            if (p.getCategoria().toLowerCase().contains(busca)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // ===================== 4. ATUALIZAÇÃO DE PRODUTOS =====================

    /**
     * Atualiza dados cadastrais do produto.
     * Apenas nome, categoria e preço (a quantidade só muda via movimentações
     * e o código é imutável, pois é a chave única do produto).
     */
    public void atualizarProduto(String codigo, String nome, String categoria, Double preco) throws EstoqueException {
        Produto produto = buscarPorCodigoObrigatorio(codigo);

        if (nome != null && !nome.trim().isEmpty()) {
            produto.setNome(nome);
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            produto.setCategoria(categoria);
        }
        if (preco != null) {
            if (preco < 0) {
                throw new EstoqueException("O preço não pode ser negativo.");
            }
            produto.setPreco(preco);
        }
    }

    // ===================== 5. REMOÇÃO DE PRODUTOS =====================

    /**
     * Remove ou inativa um produto.
     * Regra: produtos com movimentações registradas não podem ser excluídos
     * fisicamente; nesse caso, são apenas marcados como inativos.
     * Produtos sem nenhuma movimentação podem ser excluídos fisicamente.
     */
    public String removerProduto(String codigo) throws EstoqueException {
        Produto produto = buscarPorCodigoObrigatorio(codigo);

        boolean possuiMovimentacao = movimentacoes.stream()
                .anyMatch(m -> m.getCodigoProduto().equals(codigo));

        if (possuiMovimentacao) {
            produto.setAtivo(false);
            return "Produto possui histórico de movimentações. Foi marcado como INATIVO (não excluído).";
        } else {
            produtos.remove(codigo);
            return "Produto removido permanentemente do sistema (não possuía movimentações).";
        }
    }

    // ===================== 6. RELATÓRIOS =====================

    public List<Produto> relatorioEstoqueAtual() {
        return new ArrayList<>(produtos.values());
    }

    public List<Movimentacao> relatorioHistoricoMovimentacoes() {
        return new ArrayList<>(movimentacoes);
    }

    public List<Movimentacao> relatorioHistoricoPorProduto(String codigo) {
        List<Movimentacao> resultado = new ArrayList<>();
        for (Movimentacao m : movimentacoes) {
            if (m.getCodigoProduto().equals(codigo)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    // ===================== MÉTODOS AUXILIARES =====================

    private Produto buscarPorCodigoObrigatorio(String codigo) throws EstoqueException {
        Produto produto = produtos.get(codigo);
        if (produto == null) {
            throw new EstoqueException("Produto com código '" + codigo + "' não encontrado. " +
                    "Entradas/saídas só podem ser feitas para produtos previamente cadastrados.");
        }
        return produto;
    }

    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos.values());
    }
}
