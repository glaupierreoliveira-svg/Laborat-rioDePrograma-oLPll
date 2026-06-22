import java.io.Serializable;

/**
 * Classe que representa um Produto do estoque.
 * Contém os dados obrigatórios exigidos pelas regras de negócio:
 * código, nome, categoria, quantidade em estoque e preço de venda.
 */
public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private String categoria;
    private int quantidade;
    private double preco;
    private boolean ativo;
    private int estoqueMinimo;

    public Produto(String codigo, String nome, String categoria, int quantidade, double preco, int estoqueMinimo) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.preco = preco;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = true;
    }

    // ---------- Getters e Setters ----------

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    /**
     * Regra de negócio: sinaliza necessidade de reposição
     * quando o estoque está baixo (abaixo ou igual ao mínimo definido).
     */
    public boolean isEstoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    /**
     * Adiciona quantidade ao estoque (entrada de mercadoria).
     */
    public void adicionarEstoque(int qtd) {
        this.quantidade += qtd;
    }

    /**
     * Remove quantidade do estoque (saída de mercadoria).
     */
    public void removerEstoque(int qtd) {
        this.quantidade -= qtd;
    }

    @Override
    public String toString() {
        String status = ativo ? "ATIVO" : "INATIVO";
        String alerta = isEstoqueBaixo() ? " [ESTOQUE BAIXO!]" : "";
        return String.format(
                "Código: %-8s | Nome: %-20s | Categoria: %-15s | Qtd: %-6d | Preço: R$ %-10.2f | %s%s",
                codigo, nome, categoria, quantidade, preco, status, alerta
        );
    }
}
