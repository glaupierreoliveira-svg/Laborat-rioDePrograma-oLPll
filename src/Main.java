import java.util.List;
import java.util.Scanner;

/**
 * Classe principal do Sistema de Controle de Estoque para Pequeno Comércio.
 * Fornece um menu interativo via console para todas as funcionalidades:
 * cadastro, movimentações, consultas, atualização, remoção e relatórios.
 */
public class Main {

    private static final EstoqueService service = new EstoqueService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        carregarDadosExemplo();

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrarProduto();
                    case 2 -> registrarEntrada();
                    case 3 -> registrarSaida();
                    case 4 -> consultarProduto();
                    case 5 -> atualizarProduto();
                    case 6 -> removerProduto();
                    case 7 -> relatorioEstoqueAtual();
                    case 8 -> relatorioEstoqueBaixo();
                    case 9 -> relatorioHistoricoMovimentacoes();
                    case 0 -> System.out.println("\nEncerrando o sistema. Até logo!");
                    default -> System.out.println("\nOpção inválida. Tente novamente.");
                }
            } catch (EstoqueException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[ERRO INESPERADO] " + e.getMessage());
            }

            if (opcao != 0) {
                System.out.println("\nPressione ENTER para continuar...");
                scanner.nextLine();
            }

        } while (opcao != 0);

        scanner.close();
    }

    // ===================== MENU =====================

    private static void exibirMenu() {
        System.out.println("\n===========================================");
        System.out.println(" SISTEMA DE CONTROLE DE ESTOQUE - PEQUENO COMÉRCIO");
        System.out.println("===========================================");
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Registrar entrada de mercadoria");
        System.out.println("3 - Registrar saída de mercadoria");
        System.out.println("4 - Consultar produtos");
        System.out.println("5 - Atualizar produto");
        System.out.println("6 - Remover/Inativar produto");
        System.out.println("7 - Relatório: estoque atual");
        System.out.println("8 - Relatório: produtos com estoque baixo");
        System.out.println("9 - Relatório: histórico de movimentações");
        System.out.println("0 - Sair");
        System.out.println("===========================================");
    }

    // ===================== AÇÕES DO MENU =====================

    private static void cadastrarProduto() throws EstoqueException {
        System.out.println("\n--- CADASTRO DE PRODUTO ---");
        String codigo = lerTexto("Código: ");
        String nome = lerTexto("Nome: ");
        String categoria = lerTexto("Categoria: ");
        int quantidade = lerInteiro("Quantidade inicial: ");
        double preco = lerDouble("Preço de venda: ");
        int estoqueMinimo = lerInteiro("Estoque mínimo (para alerta de reposição): ");

        Produto produto = service.cadastrarProduto(codigo, nome, categoria, quantidade, preco, estoqueMinimo);
        System.out.println("\nProduto cadastrado com sucesso!");
        System.out.println(produto);
    }

    private static void registrarEntrada() throws EstoqueException {
        System.out.println("\n--- ENTRADA DE MERCADORIA ---");
        String codigo = lerTexto("Código do produto: ");
        int quantidade = lerInteiro("Quantidade recebida: ");
        String responsavel = lerTexto("Responsável pela movimentação: ");

        service.registrarEntrada(codigo, quantidade, responsavel);
        System.out.println("\nEntrada registrada com sucesso!");
        System.out.println(service.consultarPorCodigo(codigo));
    }

    private static void registrarSaida() throws EstoqueException {
        System.out.println("\n--- SAÍDA DE MERCADORIA ---");
        String codigo = lerTexto("Código do produto: ");
        int quantidade = lerInteiro("Quantidade de saída: ");
        String responsavel = lerTexto("Responsável pela movimentação: ");

        service.registrarSaida(codigo, quantidade, responsavel);
        System.out.println("\nSaída registrada com sucesso!");
        System.out.println(service.consultarPorCodigo(codigo));

        Produto produto = service.consultarPorCodigo(codigo);
        if (produto.isEstoqueBaixo()) {
            System.out.println("\n[ALERTA] Estoque baixo! Necessário providenciar reposição.");
        }
    }

    private static void consultarProduto() {
        System.out.println("\n--- CONSULTA DE PRODUTOS ---");
        System.out.println("1 - Por código");
        System.out.println("2 - Por nome");
        System.out.println("3 - Por categoria");
        int opcao = lerInteiro("Escolha o tipo de busca: ");

        switch (opcao) {
            case 1 -> {
                String codigo = lerTexto("Código: ");
                Produto produto = service.consultarPorCodigo(codigo);
                if (produto == null) {
                    System.out.println("\nNenhum produto encontrado com esse código.");
                } else {
                    System.out.println("\n" + produto);
                }
            }
            case 2 -> {
                String nome = lerTexto("Nome (ou parte do nome): ");
                imprimirListaProdutos(service.consultarPorNome(nome));
            }
            case 3 -> {
                String categoria = lerTexto("Categoria (ou parte da categoria): ");
                imprimirListaProdutos(service.consultarPorCategoria(categoria));
            }
            default -> System.out.println("\nOpção inválida.");
        }
    }

    private static void atualizarProduto() throws EstoqueException {
        System.out.println("\n--- ATUALIZAÇÃO DE PRODUTO ---");
        String codigo = lerTexto("Código do produto a atualizar: ");

        Produto produtoAtual = service.consultarPorCodigo(codigo);
        if (produtoAtual == null) {
            System.out.println("\nProduto não encontrado.");
            return;
        }
        System.out.println("Dados atuais: " + produtoAtual);
        System.out.println("(Deixe em branco para manter o valor atual)");

        String nome = lerTexto("Novo nome: ");
        String categoria = lerTexto("Nova categoria: ");
        String precoStr = lerTexto("Novo preço: ");

        Double preco = precoStr.isBlank() ? null : Double.parseDouble(precoStr.replace(",", "."));

        service.atualizarProduto(codigo, nome.isBlank() ? null : nome, categoria.isBlank() ? null : categoria, preco);
        System.out.println("\nProduto atualizado com sucesso!");
        System.out.println(service.consultarPorCodigo(codigo));
    }

    private static void removerProduto() throws EstoqueException {
        System.out.println("\n--- REMOÇÃO DE PRODUTO ---");
        String codigo = lerTexto("Código do produto: ");
        String resultado = service.removerProduto(codigo);
        System.out.println("\n" + resultado);
    }

    private static void relatorioEstoqueAtual() {
        System.out.println("\n--- RELATÓRIO: ESTOQUE ATUAL ---");
        imprimirListaProdutos(service.relatorioEstoqueAtual());
    }

    private static void relatorioEstoqueBaixo() {
        System.out.println("\n--- RELATÓRIO: PRODUTOS COM ESTOQUE BAIXO ---");
        List<Produto> lista = service.listarEstoqueBaixo();
        if (lista.isEmpty()) {
            System.out.println("Nenhum produto com estoque baixo no momento.");
        } else {
            imprimirListaProdutos(lista);
        }
    }

    private static void relatorioHistoricoMovimentacoes() {
        System.out.println("\n--- RELATÓRIO: HISTÓRICO DE MOVIMENTAÇÕES ---");
        System.out.println("1 - Histórico completo");
        System.out.println("2 - Histórico de um produto específico");
        int opcao = lerInteiro("Escolha uma opção: ");

        List<Movimentacao> historico;
        if (opcao == 2) {
            String codigo = lerTexto("Código do produto: ");
            historico = service.relatorioHistoricoPorProduto(codigo);
        } else {
            historico = service.relatorioHistoricoMovimentacoes();
        }

        if (historico.isEmpty()) {
            System.out.println("\nNenhuma movimentação encontrada.");
        } else {
            historico.forEach(System.out::println);
        }
    }

    // ===================== UTILITÁRIOS DE LEITURA =====================

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número (ex: 19.90).");
            }
        }
    }

    private static void imprimirListaProdutos(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado.");
            return;
        }
        for (Produto p : produtos) {
            System.out.println(p);
        }
    }

    // ===================== DADOS DE EXEMPLO =====================

    private static void carregarDadosExemplo() {
        try {
            service.cadastrarProduto("P001", "Arroz 5kg", "Alimentos", 50, 24.90, 10);
            service.cadastrarProduto("P002", "Detergente 500ml", "Limpeza", 30, 2.50, 15);
            service.cadastrarProduto("P003", "Caderno 200 folhas", "Papelaria", 5, 12.00, 10);
            service.registrarEntrada("P001", 20, "Admin");
            service.registrarSaida("P002", 10, "Admin");
        } catch (EstoqueException e) {
            System.out.println("Erro ao carregar dados de exemplo: " + e.getMessage());
        }
    }
}
