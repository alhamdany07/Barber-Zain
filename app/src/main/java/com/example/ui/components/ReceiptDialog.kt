package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.dao.TransactionWithItems
import com.example.data.preferences.ShopConfig

@Composable
fun ReceiptDialog(
    transactionWithItems: TransactionWithItems,
    shopConfig: ShopConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val trx = transactionWithItems.transaction
    val items = transactionWithItems.items

    val receiptText = buildString {
        appendLine("================================")
        appendLine("       ${shopConfig.shopName.uppercase()}       ")
        appendLine("  ${shopConfig.shopAddress}  ")
        appendLine("       Telp: ${shopConfig.shopPhone}       ")
        appendLine("================================")
        appendLine("No. Trx : ${trx.transactionNumber}")
        appendLine("Waktu   : ${Formatters.formatDateTime(trx.timestamp)}")
        appendLine("Kasir   : ${trx.kapsterName ?: "Admin"}")
        appendLine("Pelanggan: ${trx.customerName}")
        appendLine("--------------------------------")
        items.forEach { item ->
            appendLine("${item.itemName}")
            val sub = Formatters.formatRupiah(item.price * item.quantity)
            appendLine("  ${item.quantity} x ${Formatters.formatRupiah(item.price)}".padEnd(20) + sub.padStart(12))
        }
        appendLine("--------------------------------")
        appendLine("Subtotal:".padEnd(20) + Formatters.formatRupiah(trx.subtotal).padStart(12))
        if (trx.discountAmount > 0) {
            appendLine("Diskon:".padEnd(20) + "-${Formatters.formatRupiah(trx.discountAmount)}".padStart(12))
        }
        if (trx.tipAmount > 0) {
            appendLine("Tip:".padEnd(20) + Formatters.formatRupiah(trx.tipAmount).padStart(12))
        }
        if (trx.taxAmount > 0) {
            appendLine("Pajak:".padEnd(20) + Formatters.formatRupiah(trx.taxAmount).padStart(12))
        }
        appendLine("================================")
        appendLine("TOTAL:".padEnd(18) + Formatters.formatRupiah(trx.totalAmount).padStart(14))
        appendLine("Metode Bayar: ${trx.paymentMethod}")
        if (trx.paymentMethod == "TUNAI") {
            appendLine("Tunai   :".padEnd(20) + Formatters.formatRupiah(trx.cashGiven).padStart(12))
            appendLine("Kembali :".padEnd(20) + Formatters.formatRupiah(trx.changeAmount).padStart(12))
        }
        appendLine("================================")
        appendLine("  ${shopConfig.receiptFooter}  ")
        appendLine("================================")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Struk Pembayaran",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thermal receipt paper container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFDFEFE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Shop Header
                        Text(
                            text = shopConfig.shopName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = shopConfig.shopAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Telp: ${shopConfig.shopPhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Trx Meta
                        ReceiptRow("No. Trx", trx.transactionNumber)
                        ReceiptRow("Waktu", Formatters.formatDateTime(trx.timestamp))
                        ReceiptRow("Kasir / Kapster", trx.kapsterName ?: "Admin")
                        ReceiptRow("Pelanggan", trx.customerName)

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.itemName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = Formatters.formatRupiah(item.price * item.quantity),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "${item.quantity} x ${Formatters.formatRupiah(item.price)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Totals
                        ReceiptRow("Subtotal", Formatters.formatRupiah(trx.subtotal))
                        if (trx.discountAmount > 0) {
                            ReceiptRow("Diskon", "-${Formatters.formatRupiah(trx.discountAmount)}")
                        }
                        if (trx.tipAmount > 0) {
                            ReceiptRow("Tip", Formatters.formatRupiah(trx.tipAmount))
                        }
                        if (trx.taxAmount > 0) {
                            ReceiptRow("Pajak", Formatters.formatRupiah(trx.taxAmount))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = Formatters.formatRupiah(trx.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        ReceiptRow("Metode Bayar", trx.paymentMethod)
                        if (trx.paymentMethod == "TUNAI") {
                            ReceiptRow("Tunai", Formatters.formatRupiah(trx.cashGiven))
                            ReceiptRow("Kembali", Formatters.formatRupiah(trx.changeAmount))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = shopConfig.receiptFooter,
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Share & Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceipt(context, receiptText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("share_receipt_button"),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan Struk", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            printThermal(context, receiptText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("print_receipt_button"),
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Printer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFF0F172A))
    }
}

private fun shareReceipt(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk via")
    context.startActivity(shareIntent)
}

private fun printThermal(context: Context, text: String) {
    // Bluetooth Thermal print simulation / fallback share to printer apps (RawBT, Bluetooth Print)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Cetak ke Printer Bluetooth")
    context.startActivity(shareIntent)
}
