import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa uma movimentação de estoque (ENTRADA ou SAIDA).
 * Registra data, quantidade movimentada e responsável,
 * conforme exigido pelas regras de negócio (auditoria/histórico).
 */
public class Movimentacao implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        ENTRADA, SAIDA
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private String codigoProduto;
    private Tipo tipo;
    private int quantidade;
    private LocalDateTime data;
    private String responsavel;

    public Movimentacao(String codigoProduto, Tipo tipo, int quantidade, String responsavel) {
        this.codigoProduto = codigoProduto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.responsavel = responsavel;
        this.data = LocalDateTime.now();
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getData() {
        return data;
    }

    public String getResponsavel() {
        return responsavel;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %-7s | Produto: %-8s | Qtd: %-6d | Responsável: %s",
                data.format(FORMATTER), tipo, codigoProduto, quantidade, responsavel
        );
    }
}
