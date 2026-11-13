package com.example.data

import com.example.domain.FinancialSummary
import com.example.domain.TopSellingProduct
import com.example.entities.ProductWithVariants
import com.example.entities.SaleWithItems
import com.example.entities.StockMovementEntity
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils

object CsvExporter {

    private fun escape(value: String): String {
        val clean = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ")
        return "\"$clean\""
    }

    fun exportSalesReport(
        periodLabel: String,
        sales: List<SaleWithItems>,
        topProducts: List<TopSellingProduct>,
        lowStockProducts: List<Pair<ProductWithVariants, Int>>,
        movements: List<StockMovementEntity>,
        financial: FinancialSummary
    ): String {
        val sb = StringBuilder()
        sb.appendLine("RELATORIO COMERCIAL - GESTAO STORE")
        sb.appendLine("Periodo;${escape(periodLabel)}")
        sb.appendLine("Gerado em;${escape(DateUtils.formatDateTime(System.currentTimeMillis()))}")
        sb.appendLine()

        sb.appendLine("RESUMO FINANCEIRO")
        sb.appendLine("Indicador;Valor")
        sb.appendLine("Faturamento (Vendas);${escape(CurrencyUtils.formatCents(financial.salesRevenueCents))}")
        sb.appendLine("Sinais de Pedidos;${escape(CurrencyUtils.formatCents(financial.orderAdvancesCents))}")
        sb.appendLine("Custo Produtos Vendidos (CMV);${escape(CurrencyUtils.formatCents(financial.cogsCents))}")
        sb.appendLine("Lucro Bruto Estimado;${escape(CurrencyUtils.formatCents(financial.grossProfitCents))}")
        sb.appendLine("Despesas;${escape(CurrencyUtils.formatCents(financial.expensesCents))}")
        sb.appendLine("Saldo Financeiro;${escape(CurrencyUtils.formatCents(financial.netBalanceCents))}")
        sb.appendLine()

        sb.appendLine("VENDAS NO PERIODO")
        sb.appendLine("ID;Data;Cliente;Pagamento;Subtotal;Desconto;Total;Custo;Itens")
        sales.forEach { swi ->
            val s = swi.sale
            val itemsText = swi.items.joinToString(" | ") {
                "${it.quantity}x ${it.productNameSnapshot} (${it.colorSnapshot}/${it.sizeSnapshot})"
            }
            sb.appendLine(
                listOf(
                    s.id.toString(),
                    escape(DateUtils.formatDateTime(s.createdAtMillis)),
                    escape(s.clientNameSnapshot ?: "Consumidor avulso"),
                    escape(s.paymentMethod.label),
                    escape(CurrencyUtils.formatCents(s.subtotalCents)),
                    escape(CurrencyUtils.formatCents(s.discountCents)),
                    escape(CurrencyUtils.formatCents(s.totalCents)),
                    escape(CurrencyUtils.formatCents(s.totalCostCents)),
                    escape(itemsText)
                ).joinToString(";")
            )
        }
        sb.appendLine()

        sb.appendLine("PRODUTOS MAIS VENDIDOS")
        sb.appendLine("Codigo;Produto;Quantidade Vendida;Faturamento")
        topProducts.forEach { tp ->
            sb.appendLine(
                listOf(
                    escape(tp.productCode),
                    escape(tp.productName),
                    tp.totalQuantity.toString(),
                    escape(CurrencyUtils.formatCents(tp.totalRevenueCents))
                ).joinToString(";")
            )
        }
        sb.appendLine()

        sb.appendLine("PRODUTOS COM ESTOQUE BAIXO")
        sb.appendLine("Codigo;Produto;Categoria;Variacoes Criticas;Estoque Total")
        lowStockProducts.forEach { (pwv, minStock) ->
            val critical = pwv.variants.filter { it.quantity <= minStock }
                .joinToString(" | ") { "${it.color}/${it.size}: ${it.quantity}" }
            sb.appendLine(
                listOf(
                    escape(pwv.product.code),
                    escape(pwv.product.name),
                    escape(pwv.product.category),
                    escape(critical),
                    pwv.totalStock.toString()
                ).joinToString(";")
            )
        }
        sb.appendLine()

        sb.appendLine("HISTORICO DE MOVIMENTACOES")
        sb.appendLine("Data;Produto;Variacao;Tipo;Delta;Saldo Anterior;Novo Saldo;Motivo")
        movements.forEach { m ->
            sb.appendLine(
                listOf(
                    escape(DateUtils.formatDateTime(m.createdAtMillis)),
                    escape(m.productNameSnapshot),
                    escape(m.variantLabelSnapshot),
                    escape(m.type.label),
                    m.quantityDelta.toString(),
                    m.previousQuantity.toString(),
                    m.newQuantity.toString(),
                    escape(m.reason)
                ).joinToString(";")
            )
        }

        return sb.toString()
    }
}
