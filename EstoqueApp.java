import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// ==========================================
// 1. HIERARQUIA DE EXCEÇÕES
// ==========================================
class EstoqueException extends Exception {
    public EstoqueException(String mensagem) {
        super(mensagem);
    }
}

class QuantidadeInvalidaException extends EstoqueException {
    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}

class ProdutoIndisponivelException extends EstoqueException {
    public ProdutoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}

// ==========================================
// 2. INTERFACE
// ==========================================
interface Vendavel {
    void vender(int quantidadeDesejada) throws ProdutoIndisponivelException;
}

// ==========================================
// 3. CLASSE ABSTRATA (SUPERCLASSE)
// ==========================================
abstract class Product implements Vendavel {
    private String nome;
    private double preco;
    private int quantidade;

    public Product(String nome, double preco, int quantidade) throws QuantidadeInvalidaException {
        if (!Double.isFinite(preco) || preco < 0) {
            throw new QuantidadeInvalidaException("Preço inválido para '" + nome + "': deve ser finito e não negativo.");
        }
        if (quantidade < 0) {
            throw new QuantidadeInvalidaException("Quantidade inválida para '" + nome + "': não pode ser negativa.");
        }
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public abstract double calcularValorTotal();

    public String getDescricao() {
        return String.format("%s | Preço: R$%.2f | Quantidade: %d unid.", this.nome, this.preco, this.quantidade);
    }

    @Override
    public void vender(int quantidadeDesejada) throws ProdutoIndisponivelException {
        if (quantidadeDesejada > this.quantidade) {
            throw new ProdutoIndisponivelException(
                "Estoque insuficiente de " + this.nome + 
                ". Solicitado: " + quantidadeDesejada + 
                " | Disponível: " + this.quantidade
            );
        }
        this.quantidade -= quantidadeDesejada;
    }

    // Sobrecarga: Polimorfismo Estático
    public void aplicarDesconto(double percentual) {
        if (percentual > 0 && percentual <= 100) {
            this.preco -= this.preco * (percentual / 100.0);
        }
    }

    public void aplicarDesconto(double percentual, double descontoMaximo) {
        if (percentual > 0 && percentual <= 100 && descontoMaximo > 0) {
            double valorDesconto = this.preco * (percentual / 100.0);
            if (valorDesconto > descontoMaximo) {
                valorDesconto = descontoMaximo;
            }
            this.preco -= valorDesconto;
        }
    }

    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public int getQuantidade() { return quantidade; }
}

// ==========================================
// 4. SUBCLASSES (POLIMORFISMO DINÂMICO)
// ==========================================
class ProdutoComum extends Product {
    public ProdutoComum(String nome, double preco, int quantidade) throws QuantidadeInvalidaException {
        super(nome, preco, quantidade);
    }

    @Override
    public double calcularValorTotal() {
        return getPreco() * getQuantidade();
    }

    @Override
    public String getDescricao() {
        return "[Comum] " + super.getDescricao();
    }
}

class ProdutoPerecivel extends Product {
    private int diasParaVencer;

    public ProdutoPerecivel(String nome, double preco, int quantidade, int diasParaVencer) throws QuantidadeInvalidaException {
        super(nome, preco, quantidade);
        if (diasParaVencer < 0) {
            throw new QuantidadeInvalidaException("Dias para vencer inválidos para '" + nome + "': não podem ser negativos.");
        }
        this.diasParaVencer = diasParaVencer;
    }

    @Override
    public double calcularValorTotal() {
        double valorBase = getPreco() * getQuantidade();
        if (this.diasParaVencer <= 3) {
            return valorBase * 0.80; // 20% de desconto
        }
        return valorBase;
    }

    @Override
    public String getDescricao() {
        return "[Perecível] " + super.getDescricao() + " | Vence em: " + this.diasParaVencer + " dias";
    }
}

// ==========================================
// 5. CLASSE DE GERENCIAMENTO (COMPOSIÇÃO)
// ==========================================
class Estoque {
    private List<Product> produtos;

    public Estoque() {
        this.produtos = new ArrayList<>();
    }

    public void adicionarProduto(Product p) {
        if (p != null) {
            this.produtos.add(p);
        }
    }

    public void venderProduto(int indice, int quantidade) throws EstoqueException {
        if (indice >= 0 && indice < produtos.size()) {
            produtos.get(indice).vender(quantidade);
        } else {
            throw new EstoqueException("Índice de produto inválido: " + indice);
        }
    }

    public double calcularValorTotalEstoque() {
        double total = 0.0;
        for (Product p : produtos) {
            total += p.calcularValorTotal();
        }
        return total;
    }

    public void exibirEstoque() {
        System.out.println("\n--- LISTA DE PRODUTOS CADASTRADOS ---");
        for (int i = 0; i < produtos.size(); i++) {
            System.out.println("ID [" + i + "] " + produtos.get(i).getDescricao());
        }
        System.out.println("-------------------------------------");
    }
}

// ==========================================
// 6. CLASSE PRINCIPAL (APP)
// ==========================================
public class EstoqueApp {
    public static void main(String[] args) {
        Estoque estoque = new Estoque();

        System.out.println(">>> 1. CAPTURANDO EXCEÇÃO DE QUANTIDADE INVÁLIDA");
try {
    // Tenta cadastrar um produto com quantidade negativa (-5)
    Product falha = new ProdutoComum("Teclado", 150.0, -5);
    estoque.adicionarProduto(falha);
} catch (QuantidadeInvalidaException e) {
    System.out.println("[ERRO TRATADO] " + e.getMessage());
}

        System.out.println("\n>>> 2. CADASTRANDO PRODUTOS VÁLIDOS");
        try {
            estoque.adicionarProduto(new ProdutoComum("Caderno", 20.0, 50));
            estoque.adicionarProduto(new ProdutoComum("Caneta", 3.0, 100));
            
            // Perecível sem desconto (> 3 dias)
            estoque.adicionarProduto(new ProdutoPerecivel("Leite", 5.0, 40, 10)); 
            
            // Perecível COM desconto de 20% (<= 3 dias)
            estoque.adicionarProduto(new ProdutoPerecivel("Morango", 10.0, 20, 2)); 
            
            System.out.println("Produtos cadastrados com sucesso!");
        } catch (EstoqueException e) {
            System.out.println(e.getMessage());
        }

        // Mostra os produtos antes das vendas
        estoque.exibirEstoque();

        System.out.println("\n>>> 3. REALIZANDO VENDA BEM-SUCEDIDA");
        try {
            System.out.println("Vendendo 10 Cadernos (ID 0)...");
            estoque.venderProduto(0, 10);
            System.out.println("[SUCESSO] Venda realizada. Estoque do Caderno atualizado.");
        } catch (EstoqueException e) {
            System.out.println("[ERRO TRATADO] " + e.getMessage());
        }

        System.out.println("\n>>> 4. CAPTURANDO EXCEÇÃO DE PRODUTO INDISPONÍVEL (FALTA DE ESTOQUE)");
        try {
            System.out.println("Tentando vender 200 Canetas (ID 1), mas só temos 100...");
            estoque.venderProduto(1, 200);
        } catch (QuantidadeInvalidaException e) {
            System.out.println("[ERRO] " + e.getMessage());
        } catch (ProdutoIndisponivelException e) {
            System.out.println("[ERRO TRATADO] " + e.getMessage());
        } catch (EstoqueException e) {
            System.out.println("[ERRO GENÉRICO] " + e.getMessage());
        }

        System.out.println("\n>>> 5. EXIBINDO ESTOQUE FINAL E VALOR TOTAL");
        estoque.exibirEstoque();
        
        // Cálculo do valor esperado:
        // Caderno: (50 - 10) * 20.0 = R$ 800.00
        // Caneta: 100 * 3.0 = R$ 300.00
        // Leite: 40 * 5.0 = R$ 200.00
        // Morango: 20 * 10.0 = 200 -> Com 20% desconto = R$ 160.00
        // Total esperado: 800 + 300 + 200 + 160 = R$ 1460.00
        
        System.out.printf(Locale.US, "VALOR TOTAL DO ESTOQUE: R$%.2f\n", estoque.calcularValorTotalEstoque());
    }
}
