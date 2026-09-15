package com.example.ui.converters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.converters.engine.FinanceCalculators
import com.example.converters.model.ConverterType
import com.example.ui.components.GlassSurface

@Composable
fun FinanceScreen(
    converterType: ConverterType,
    modifier: Modifier = Modifier
) {
    when (converterType) {
        ConverterType.LOAN_EMI -> LoanEmiView(modifier)
        ConverterType.PURCHASE_EMI_GST -> PurchaseEmiGstView(modifier)
        ConverterType.SIP -> SipView(modifier)
        ConverterType.SWP -> SwpView(modifier)
        ConverterType.EPF -> EpfView(modifier)
        ConverterType.PPF -> PpfView(modifier)
        ConverterType.NPS -> NpsView(modifier)
        ConverterType.FD -> FdView(modifier)
        ConverterType.RD -> RdView(modifier)
        ConverterType.COMPOUND_INTEREST -> CompoundInterestView(modifier)
        ConverterType.DISCOUNT -> DiscountView(modifier)
        ConverterType.ROI -> RoiView(modifier)
        ConverterType.GST -> GstView(modifier)
        else -> Text("Finance tool coming soon", color = Color.White)
    }
}

// 31. Loan EMI
@Composable
private fun LoanEmiView(modifier: Modifier = Modifier) {
    var principalStr by remember { mutableStateOf("1000000") }
    var interestStr by remember { mutableStateOf("8.5") }
    var tenureStr by remember { mutableStateOf("60") } // months

    val result = remember(principalStr, interestStr, tenureStr) {
        val p = principalStr.toDoubleOrNull() ?: 0.0
        val r = interestStr.toDoubleOrNull() ?: 0.0
        val t = tenureStr.toIntOrNull() ?: 12
        FinanceCalculators.calculateLoanEmi(p, r, t)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Loan Amount (₹)", principalStr, { principalStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Interest Rate (%)", interestStr, { interestStr = it }, Modifier.weight(1f))
            FinanceTextField("Tenure (Months)", tenureStr, { tenureStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Monthly EMI", FinanceCalculators.formatMoneyRound(result.monthlyEmi))
        SpecialResultCard("Total Interest Payable", FinanceCalculators.formatMoneyRound(result.totalInterest))
        SpecialResultCard("Total Payment (Principal + Interest)", FinanceCalculators.formatMoneyRound(result.totalPayment))
    }
}

// 32. Purchase EMI (with GST)
@Composable
private fun PurchaseEmiGstView(modifier: Modifier = Modifier) {
    var priceStr by remember { mutableStateOf("80000") }
    var downPayStr by remember { mutableStateOf("10000") }
    var interestStr by remember { mutableStateOf("15") }
    var tenureStr by remember { mutableStateOf("12") }

    val result = remember(priceStr, downPayStr, interestStr, tenureStr) {
        val pr = priceStr.toDoubleOrNull() ?: 0.0
        val dp = downPayStr.toDoubleOrNull() ?: 0.0
        val r = interestStr.toDoubleOrNull() ?: 0.0
        val t = tenureStr.toIntOrNull() ?: 6
        FinanceCalculators.calculatePurchaseEmiWithGst(pr, dp, r, t)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Product Price (₹)", priceStr, { priceStr = it })
        FinanceTextField("Down Payment (₹)", downPayStr, { downPayStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Annual Interest %", interestStr, { interestStr = it }, Modifier.weight(1f))
            FinanceTextField("Tenure (Months)", tenureStr, { tenureStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Total Monthly EMI (incl. 18% GST)", FinanceCalculators.formatMoney(result.totalMonthlyEmi))
        SpecialResultCard("Base EMI + Monthly GST", "${FinanceCalculators.formatMoney(result.baseMonthlyEmi)} + ${FinanceCalculators.formatMoney(result.monthlyGstOnInterest)}")
        SpecialResultCard("Total Loan Interest + GST", "${FinanceCalculators.formatMoney(result.totalInterest)} + ${FinanceCalculators.formatMoney(result.totalGstAmount)}")
        SpecialResultCard("Total Effective Cost", FinanceCalculators.formatMoney(result.totalCost))
    }
}

// 33. SIP
@Composable
private fun SipView(modifier: Modifier = Modifier) {
    var monthlyStr by remember { mutableStateOf("5000") }
    var returnStr by remember { mutableStateOf("12") }
    var tenureYearsStr by remember { mutableStateOf("10") }

    val result = remember(monthlyStr, returnStr, tenureYearsStr) {
        val m = monthlyStr.toDoubleOrNull() ?: 0.0
        val r = returnStr.toDoubleOrNull() ?: 0.0
        val t = tenureYearsStr.toDoubleOrNull() ?: 1.0
        FinanceCalculators.calculateSip(m, r, t)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Monthly Investment (₹)", monthlyStr, { monthlyStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Expected Return (%)", returnStr, { returnStr = it }, Modifier.weight(1f))
            FinanceTextField("Time Period (Years)", tenureYearsStr, { tenureYearsStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Total Invested", FinanceCalculators.formatMoneyRound(result.investedAmount))
        SpecialResultCard("Estimated Wealth Gain", FinanceCalculators.formatMoneyRound(result.estimatedReturns))
        SpecialResultCard("Expected Total Corpus", FinanceCalculators.formatMoneyRound(result.totalValue))
    }
}

// 34. SWP
@Composable
private fun SwpView(modifier: Modifier = Modifier) {
    var initialStr by remember { mutableStateOf("1000000") }
    var withdrawalStr by remember { mutableStateOf("10000") }
    var returnStr by remember { mutableStateOf("8.5") }
    var yearsStr by remember { mutableStateOf("10") }

    val result = remember(initialStr, withdrawalStr, returnStr, yearsStr) {
        val init = initialStr.toDoubleOrNull() ?: 0.0
        val w = withdrawalStr.toDoubleOrNull() ?: 0.0
        val r = returnStr.toDoubleOrNull() ?: 0.0
        val y = yearsStr.toDoubleOrNull() ?: 5.0
        FinanceCalculators.calculateSwp(init, w, r, y)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Initial Investment (₹)", initialStr, { initialStr = it })
        FinanceTextField("Monthly Withdrawal (₹)", withdrawalStr, { withdrawalStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Return Rate (%)", returnStr, { returnStr = it }, Modifier.weight(1f))
            FinanceTextField("Duration (Years)", yearsStr, { yearsStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Total Amount Withdrawn", FinanceCalculators.formatMoneyRound(result.totalWithdrawn))
        SpecialResultCard("Remaining Balance", FinanceCalculators.formatMoneyRound(result.remainingBalance))
        SpecialResultCard("Months Lasted", "${result.monthsLasted} Months")
    }
}

// 35. EPF
@Composable
private fun EpfView(modifier: Modifier = Modifier) {
    var salaryStr by remember { mutableStateOf("45000") }
    var balanceStr by remember { mutableStateOf("200000") }
    var yearsStr by remember { mutableStateOf("25") }

    val result = remember(salaryStr, balanceStr, yearsStr) {
        val s = salaryStr.toDoubleOrNull() ?: 0.0
        val b = balanceStr.toDoubleOrNull() ?: 0.0
        val y = yearsStr.toIntOrNull() ?: 20
        FinanceCalculators.calculateEpf(s, b, yearsToRetire = y)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Monthly Basic + DA (₹)", salaryStr, { salaryStr = it })
        FinanceTextField("Current EPF Balance (₹)", balanceStr, { balanceStr = it })
        FinanceTextField("Years to Retirement", yearsStr, { yearsStr = it })

        SpecialResultCard("Final EPF Retirement Corpus", FinanceCalculators.formatMoneyRound(result.maturityCorpus))
        SpecialResultCard("Total Interest Earned", FinanceCalculators.formatMoneyRound(result.totalInterest))
        SpecialResultCard("Total Contributions", "${FinanceCalculators.formatMoneyRound(result.totalEmployeeContrib)} (Emp) + ${FinanceCalculators.formatMoneyRound(result.totalEmployerContrib)} (Employer)")
    }
}

// 36. PPF
@Composable
private fun PpfView(modifier: Modifier = Modifier) {
    var yearlyStr by remember { mutableStateOf("150000") }
    var yearsStr by remember { mutableStateOf("15") }

    val result = remember(yearlyStr, yearsStr) {
        val dep = yearlyStr.toDoubleOrNull() ?: 10000.0
        val y = yearsStr.toIntOrNull() ?: 15
        FinanceCalculators.calculatePpf(dep, tenureYears = y)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Yearly Deposit (Max ₹1.5L)", yearlyStr, { yearlyStr = it })
        FinanceTextField("Tenure Years (Default 15)", yearsStr, { yearsStr = it })

        SpecialResultCard("Total PPF Maturity Amount", FinanceCalculators.formatMoneyRound(result.maturityAmount))
        SpecialResultCard("Total Invested", FinanceCalculators.formatMoneyRound(result.totalInvested))
        SpecialResultCard("Total Tax-Free Interest Earned", FinanceCalculators.formatMoneyRound(result.totalInterest))
    }
}

// 37. NPS
@Composable
private fun NpsView(modifier: Modifier = Modifier) {
    var ageStr by remember { mutableStateOf("28") }
    var monthlyStr by remember { mutableStateOf("5000") }
    var returnStr by remember { mutableStateOf("10") }

    val result = remember(ageStr, monthlyStr, returnStr) {
        val a = ageStr.toIntOrNull() ?: 30
        val m = monthlyStr.toDoubleOrNull() ?: 2000.0
        val r = returnStr.toDoubleOrNull() ?: 10.0
        FinanceCalculators.calculateNps(a, monthlyContribution = m, expectedReturnRate = r)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Current Age", ageStr, { ageStr = it }, Modifier.weight(1f))
            FinanceTextField("Retirement Age", "60", {}, Modifier.weight(1f))
        }
        FinanceTextField("Monthly Contribution (₹)", monthlyStr, { monthlyStr = it })

        SpecialResultCard("Total Pension Wealth", FinanceCalculators.formatMoneyRound(result.totalPensionWealth))
        SpecialResultCard("Lump Sum Withdrawal (60%)", FinanceCalculators.formatMoneyRound(result.lumpSumAmount))
        SpecialResultCard("Estimated Monthly Pension", FinanceCalculators.formatMoneyRound(result.expectedMonthlyPension))
    }
}

// 38. FD
@Composable
private fun FdView(modifier: Modifier = Modifier) {
    var principalStr by remember { mutableStateOf("500000") }
    var rateStr by remember { mutableStateOf("7.25") }
    var yearsStr by remember { mutableStateOf("5") }

    val result = remember(principalStr, rateStr, yearsStr) {
        val p = principalStr.toDoubleOrNull() ?: 0.0
        val r = rateStr.toDoubleOrNull() ?: 0.0
        val y = yearsStr.toDoubleOrNull() ?: 1.0
        FinanceCalculators.calculateFd(p, r, y)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Deposit Amount (₹)", principalStr, { principalStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Interest Rate %", rateStr, { rateStr = it }, Modifier.weight(1f))
            FinanceTextField("Tenure (Years)", yearsStr, { yearsStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Maturity Value (Quarterly Compounding)", FinanceCalculators.formatMoneyRound(result.maturityAmount))
        SpecialResultCard("Total Interest Earned", FinanceCalculators.formatMoneyRound(result.totalInterest))
    }
}

// 39. RD
@Composable
private fun RdView(modifier: Modifier = Modifier) {
    var monthlyStr by remember { mutableStateOf("5000") }
    var rateStr by remember { mutableStateOf("7.0") }
    var monthsStr by remember { mutableStateOf("36") }

    val result = remember(monthlyStr, rateStr, monthsStr) {
        val m = monthlyStr.toDoubleOrNull() ?: 0.0
        val r = rateStr.toDoubleOrNull() ?: 0.0
        val t = monthsStr.toIntOrNull() ?: 12
        FinanceCalculators.calculateRd(m, r, t)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Monthly Installment (₹)", monthlyStr, { monthlyStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Interest Rate %", rateStr, { rateStr = it }, Modifier.weight(1f))
            FinanceTextField("Tenure (Months)", monthsStr, { monthsStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("RD Maturity Value", FinanceCalculators.formatMoneyRound(result.maturityAmount))
        SpecialResultCard("Total Deposited", FinanceCalculators.formatMoneyRound(result.totalDeposited))
        SpecialResultCard("Total Interest Earned", FinanceCalculators.formatMoneyRound(result.totalInterest))
    }
}

// 40. Compound Interest
@Composable
private fun CompoundInterestView(modifier: Modifier = Modifier) {
    var principalStr by remember { mutableStateOf("100000") }
    var rateStr by remember { mutableStateOf("10") }
    var yearsStr by remember { mutableStateOf("5") }

    val result = remember(principalStr, rateStr, yearsStr) {
        val p = principalStr.toDoubleOrNull() ?: 0.0
        val r = rateStr.toDoubleOrNull() ?: 0.0
        val y = yearsStr.toDoubleOrNull() ?: 1.0
        FinanceCalculators.calculateCompoundInterest(p, r, y, 1)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Principal Amount (₹)", principalStr, { principalStr = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinanceTextField("Annual Rate %", rateStr, { rateStr = it }, Modifier.weight(1f))
            FinanceTextField("Time (Years)", yearsStr, { yearsStr = it }, Modifier.weight(1f))
        }

        SpecialResultCard("Total Amount", FinanceCalculators.formatMoneyRound(result.totalAmount))
        SpecialResultCard("Compound Interest", FinanceCalculators.formatMoneyRound(result.compoundInterest))
    }
}

// 41. Discount
@Composable
private fun DiscountView(modifier: Modifier = Modifier) {
    var priceStr by remember { mutableStateOf("2499") }
    var discountStr by remember { mutableStateOf("25") }

    val result = remember(priceStr, discountStr) {
        val p = priceStr.toDoubleOrNull() ?: 0.0
        val d = discountStr.toDoubleOrNull() ?: 0.0
        FinanceCalculators.calculateDiscount(p, d)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Original Price (₹)", priceStr, { priceStr = it })
        FinanceTextField("Discount (%)", discountStr, { discountStr = it })

        SpecialResultCard("Final Price to Pay", FinanceCalculators.formatMoney(result.finalPrice))
        SpecialResultCard("You Save", FinanceCalculators.formatMoney(result.totalSavings))
    }
}

// 42. ROI
@Composable
private fun RoiView(modifier: Modifier = Modifier) {
    var initStr by remember { mutableStateOf("200000") }
    var finalStr by remember { mutableStateOf("320000") }
    var yearsStr by remember { mutableStateOf("3") }

    val result = remember(initStr, finalStr, yearsStr) {
        val i = initStr.toDoubleOrNull() ?: 0.0
        val f = finalStr.toDoubleOrNull() ?: 0.0
        val y = yearsStr.toDoubleOrNull() ?: 1.0
        FinanceCalculators.calculateRoi(i, f, y)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FinanceTextField("Initial Investment (₹)", initStr, { initStr = it })
        FinanceTextField("Final Value (₹)", finalStr, { finalStr = it })
        FinanceTextField("Duration (Years)", yearsStr, { yearsStr = it })

        SpecialResultCard("Net Profit", FinanceCalculators.formatMoneyRound(result.netProfit))
        SpecialResultCard("Total ROI", "${String.format("%.2f", result.totalRoiPercent)}%")
        SpecialResultCard("Annualized ROI (CAGR)", "${String.format("%.2f", result.annualizedCagrPercent)}%")
    }
}

// 43. GST
@Composable
private fun GstView(modifier: Modifier = Modifier) {
    var amountStr by remember { mutableStateOf("10000") }
    var selectedSlab by remember { mutableStateOf(18.0) }
    var isAddGst by remember { mutableStateOf(true) }

    val result = remember(amountStr, selectedSlab, isAddGst) {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        FinanceCalculators.calculateGst(amt, selectedSlab, isAddGst)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isAddGst,
                onClick = { isAddGst = true },
                label = { Text("Add GST (Net → Gross)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2563EB), selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = !isAddGst,
                onClick = { isAddGst = false },
                label = { Text("Remove GST (Gross → Net)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2563EB), selectedLabelColor = Color.White)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(5.0, 12.0, 18.0, 28.0).forEach { rate ->
                FilterChip(
                    selected = selectedSlab == rate,
                    onClick = { selectedSlab = rate },
                    label = { Text("${rate.toInt()}%") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF38BDF8), selectedLabelColor = Color.Black)
                )
            }
        }

        FinanceTextField(if (isAddGst) "Base Amount (₹)" else "Gross Amount (₹)", amountStr, { amountStr = it })

        SpecialResultCard("Gross Total Amount", FinanceCalculators.formatMoney(result.grossAmount))
        SpecialResultCard("Total GST (${result.gstPercent.toInt()}%)", FinanceCalculators.formatMoney(result.totalGstAmount))
        SpecialResultCard("CGST + SGST Breakdown", "${FinanceCalculators.formatMoney(result.cgstAmount)} (CGST) + ${FinanceCalculators.formatMoney(result.sgstAmount)} (SGST)")
        SpecialResultCard("Net Base Amount", FinanceCalculators.formatMoney(result.baseAmount))
    }
}

@Composable
private fun FinanceTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
            focusedBorderColor = Color(0xFF38BDF8),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
        ),
        modifier = modifier.fillMaxWidth()
    )
}
