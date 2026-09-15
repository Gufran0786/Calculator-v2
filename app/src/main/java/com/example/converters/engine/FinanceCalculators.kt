package com.example.converters.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

object FinanceCalculators {

    private val currencyFmt = DecimalFormat("#,##,##0.00", DecimalFormatSymbols(Locale.US))
    private val intCurrencyFmt = DecimalFormat("#,##,##0", DecimalFormatSymbols(Locale.US))

    fun formatMoney(amount: Double): String {
        return if (amount >= 0) "₹ ${currencyFmt.format(amount)}" else "-₹ ${currencyFmt.format(kotlin.math.abs(amount))}"
    }

    fun formatMoneyRound(amount: Double): String {
        return if (amount >= 0) "₹ ${intCurrencyFmt.format(amount)}" else "-₹ ${intCurrencyFmt.format(kotlin.math.abs(amount))}"
    }

    // 31. Loan EMI
    data class LoanEmiResult(
        val monthlyEmi: Double,
        val totalInterest: Double,
        val totalPayment: Double,
        val principalPercent: Float,
        val interestPercent: Float
    )

    fun calculateLoanEmi(principal: Double, annualRate: Double, tenureMonths: Int): LoanEmiResult {
        if (principal <= 0 || tenureMonths <= 0) return LoanEmiResult(0.0, 0.0, 0.0, 100f, 0f)
        if (annualRate <= 0) {
            val emi = principal / tenureMonths
            return LoanEmiResult(emi, 0.0, principal, 100f, 0f)
        }
        val r = annualRate / (12.0 * 100.0)
        val power = (1.0 + r).pow(tenureMonths.toDouble())
        val emi = (principal * r * power) / (power - 1.0)
        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal

        val pPct = ((principal / totalPayment) * 100).toFloat().coerceIn(0f, 100f)
        val iPct = (100f - pPct).coerceIn(0f, 100f)

        return LoanEmiResult(emi, totalInterest, totalPayment, pPct, iPct)
    }

    // 32. Purchase EMI (with GST on interest)
    data class PurchaseEmiResult(
        val loanAmount: Double,
        val baseMonthlyEmi: Double,
        val monthlyGstOnInterest: Double,
        val totalMonthlyEmi: Double,
        val totalInterest: Double,
        val totalGstAmount: Double,
        val totalCost: Double
    )

    fun calculatePurchaseEmiWithGst(
        productPrice: Double,
        downPayment: Double,
        annualRate: Double,
        tenureMonths: Int,
        gstRatePercent: Double = 18.0
    ): PurchaseEmiResult {
        val loanAmount = (productPrice - downPayment).coerceAtLeast(0.0)
        if (loanAmount <= 0 || tenureMonths <= 0) {
            return PurchaseEmiResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, productPrice)
        }

        val baseResult = calculateLoanEmi(loanAmount, annualRate, tenureMonths)
        val totalGstOnInterest = baseResult.totalInterest * (gstRatePercent / 100.0)
        val monthlyGst = totalGstOnInterest / tenureMonths
        val effectiveMonthlyEmi = baseResult.monthlyEmi + monthlyGst
        val totalEffectiveCost = downPayment + loanAmount + baseResult.totalInterest + totalGstOnInterest

        return PurchaseEmiResult(
            loanAmount = loanAmount,
            baseMonthlyEmi = baseResult.monthlyEmi,
            monthlyGstOnInterest = monthlyGst,
            totalMonthlyEmi = effectiveMonthlyEmi,
            totalInterest = baseResult.totalInterest,
            totalGstAmount = totalGstOnInterest,
            totalCost = totalEffectiveCost
        )
    }

    // 33. SIP (Systematic Investment Plan)
    data class SipResult(
        val investedAmount: Double,
        val estimatedReturns: Double,
        val totalValue: Double
    )

    fun calculateSip(monthlyInvestment: Double, expectedAnnualReturn: Double, tenureYears: Double): SipResult {
        if (monthlyInvestment <= 0 || tenureYears <= 0) return SipResult(0.0, 0.0, 0.0)
        val months = (tenureYears * 12).toInt()
        val r = expectedAnnualReturn / (12.0 * 100.0)
        val totalValue = if (r > 0) {
            monthlyInvestment * (((1.0 + r).pow(months.toDouble()) - 1.0) / r) * (1.0 + r)
        } else {
            monthlyInvestment * months
        }
        val invested = monthlyInvestment * months
        val returns = (totalValue - invested).coerceAtLeast(0.0)
        return SipResult(invested, returns, totalValue)
    }

    // 34. SWP (Systematic Withdrawal Plan)
    data class SwpResult(
        val totalWithdrawn: Double,
        val remainingBalance: Double,
        val monthsLasted: Int
    )

    fun calculateSwp(
        initialInvestment: Double,
        monthlyWithdrawal: Double,
        annualReturnRate: Double,
        durationYears: Double
    ): SwpResult {
        val totalMonths = (durationYears * 12).toInt()
        val monthlyRate = annualReturnRate / (12.0 * 100.0)
        var balance = initialInvestment
        var withdrawn = 0.0
        var monthsLasted = 0

        for (m in 1..totalMonths) {
            if (balance <= 0) break
            balance += balance * monthlyRate
            val w = monthlyWithdrawal.coerceAtMost(balance)
            balance -= w
            withdrawn += w
            monthsLasted = m
        }

        return SwpResult(withdrawn, balance.coerceAtLeast(0.0), monthsLasted)
    }

    // 35. EPF (Employee Provident Fund)
    data class EpfResult(
        val totalEmployeeContrib: Double,
        val totalEmployerContrib: Double,
        val totalInterest: Double,
        val maturityCorpus: Double
    )

    fun calculateEpf(
        monthlyBasicDa: Double,
        currentBalance: Double,
        employeeSharePercent: Double = 12.0,
        employerSharePercent: Double = 3.67, // 3.67% to EPF (remainder 8.33% goes to EPS)
        interestRate: Double = 8.25,
        yearsToRetire: Int = 20,
        annualSalaryHikePercent: Double = 5.0
    ): EpfResult {
        var balance = currentBalance
        var salary = monthlyBasicDa
        var totalEmployee = 0.0
        var totalEmployer = 0.0
        val r = interestRate / 100.0

        for (y in 1..yearsToRetire) {
            val empMonthly = salary * (employeeSharePercent / 100.0)
            val emprMonthly = salary * (employerSharePercent / 100.0)
            val yearlyEmp = empMonthly * 12.0
            val yearlyEmpr = emprMonthly * 12.0

            totalEmployee += yearlyEmp
            totalEmployer += yearlyEmpr

            val interest = (balance + (yearlyEmp + yearlyEmpr) / 2.0) * r
            balance += yearlyEmp + yearlyEmpr + interest

            salary += salary * (annualSalaryHikePercent / 100.0)
        }

        val totalInterest = balance - (currentBalance + totalEmployee + totalEmployer)
        return EpfResult(totalEmployee, totalEmployer, totalInterest.coerceAtLeast(0.0), balance)
    }

    // 36. PPF (Public Provident Fund)
    data class PpfResult(
        val totalInvested: Double,
        val totalInterest: Double,
        val maturityAmount: Double
    )

    fun calculatePpf(yearlyDeposit: Double, interestRate: Double = 7.1, tenureYears: Int = 15): PpfResult {
        var balance = 0.0
        var invested = 0.0
        val r = interestRate / 100.0

        for (y in 1..tenureYears) {
            val deposit = yearlyDeposit.coerceAtMost(150000.0)
            invested += deposit
            val interest = (balance + deposit) * r
            balance += deposit + interest
        }
        return PpfResult(invested, balance - invested, balance)
    }

    // 37. NPS (National Pension Scheme)
    data class NpsResult(
        val totalInvested: Double,
        val totalPensionWealth: Double,
        val lumpSumAmount: Double,
        val annuityCorpus: Double,
        val expectedMonthlyPension: Double
    )

    fun calculateNps(
        currentAge: Int,
        retirementAge: Int = 60,
        monthlyContribution: Double,
        expectedReturnRate: Double = 10.0,
        annuityPercent: Double = 40.0, // Min 40% mandatory
        annuityRoiPercent: Double = 6.0
    ): NpsResult {
        val years = (retirementAge - currentAge).coerceAtLeast(1)
        val sipResult = calculateSip(monthlyContribution, expectedReturnRate, years.toDouble())

        val wealth = sipResult.totalValue
        val annuityCorpus = wealth * (annuityPercent / 100.0)
        val lumpSum = wealth - annuityCorpus
        val monthlyPension = (annuityCorpus * (annuityRoiPercent / 100.0)) / 12.0

        return NpsResult(
            totalInvested = sipResult.investedAmount,
            totalPensionWealth = wealth,
            lumpSumAmount = lumpSum,
            annuityCorpus = annuityCorpus,
            expectedMonthlyPension = monthlyPension
        )
    }

    // 38. FD (Fixed Deposit)
    data class FdResult(
        val principal: Double,
        val totalInterest: Double,
        val maturityAmount: Double
    )

    fun calculateFd(
        principal: Double,
        annualRate: Double,
        tenureYears: Double,
        compoundingPerYear: Int = 4 // standard quarterly in India
    ): FdResult {
        val r = annualRate / 100.0
        val n = compoundingPerYear.coerceAtLeast(1)
        val maturity = principal * (1.0 + r / n).pow(n * tenureYears)
        return FdResult(principal, maturity - principal, maturity)
    }

    // 39. RD (Recurring Deposit)
    data class RdResult(
        val totalDeposited: Double,
        val totalInterest: Double,
        val maturityAmount: Double
    )

    fun calculateRd(
        monthlyDeposit: Double,
        annualRate: Double,
        tenureMonths: Int
    ): RdResult {
        // Quarter compounding formula for Indian RD
        val i = (annualRate / 100.0) / 4.0
        var maturity = 0.0
        for (m in 1..tenureMonths) {
            val quarters = (tenureMonths - m + 1) / 3.0
            maturity += monthlyDeposit * (1.0 + i).pow(quarters)
        }
        val deposited = monthlyDeposit * tenureMonths
        return RdResult(deposited, maturity - deposited, maturity)
    }

    // 40. Compound Interest
    data class CiResult(
        val principal: Double,
        val compoundInterest: Double,
        val totalAmount: Double
    )

    fun calculateCompoundInterest(
        principal: Double,
        annualRate: Double,
        tenureYears: Double,
        frequencyPerYear: Int = 1 // 1=Annually, 2=Half, 4=Quarterly, 12=Monthly, 365=Daily
    ): CiResult {
        val r = annualRate / 100.0
        val n = frequencyPerYear.coerceAtLeast(1)
        val amount = principal * (1.0 + r / n).pow(n * tenureYears)
        return CiResult(principal, amount - principal, amount)
    }

    // 41. Discount Calculator
    data class DiscountResult(
        val originalPrice: Double,
        val discountAmount: Double,
        val priceAfterDiscount: Double,
        val taxAmount: Double,
        val finalPrice: Double,
        val totalSavings: Double
    )

    fun calculateDiscount(
        originalPrice: Double,
        discountPercent: Double,
        taxPercent: Double = 0.0
    ): DiscountResult {
        val discountAmount = originalPrice * (discountPercent / 100.0)
        val afterDiscount = (originalPrice - discountAmount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * (taxPercent / 100.0)
        val finalPrice = afterDiscount + taxAmount
        val totalSavings = discountAmount

        return DiscountResult(
            originalPrice = originalPrice,
            discountAmount = discountAmount,
            priceAfterDiscount = afterDiscount,
            taxAmount = taxAmount,
            finalPrice = finalPrice,
            totalSavings = totalSavings
        )
    }

    // 42. ROI (Return on Investment)
    data class RoiResult(
        val netProfit: Double,
        val totalRoiPercent: Double,
        val annualizedCagrPercent: Double
    )

    fun calculateRoi(
        initialInvestment: Double,
        finalValue: Double,
        durationYears: Double
    ): RoiResult {
        val profit = finalValue - initialInvestment
        val totalRoi = if (initialInvestment > 0) (profit / initialInvestment) * 100.0 else 0.0
        val cagr = if (initialInvestment > 0 && finalValue > 0 && durationYears > 0) {
            ((finalValue / initialInvestment).pow(1.0 / durationYears) - 1.0) * 100.0
        } else {
            0.0
        }
        return RoiResult(profit, totalRoi, cagr)
    }

    // 43. GST Calculator
    data class GstResult(
        val baseAmount: Double,
        val gstPercent: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val totalGstAmount: Double,
        val grossAmount: Double
    )

    fun calculateGst(amount: Double, gstRate: Double, isAddGst: Boolean): GstResult {
        return if (isAddGst) {
            val totalGst = amount * (gstRate / 100.0)
            val halfGst = totalGst / 2.0
            GstResult(
                baseAmount = amount,
                gstPercent = gstRate,
                cgstAmount = halfGst,
                sgstAmount = halfGst,
                totalGstAmount = totalGst,
                grossAmount = amount + totalGst
            )
        } else {
            // Remove GST (amount is gross)
            val base = amount / (1.0 + gstRate / 100.0)
            val totalGst = amount - base
            val halfGst = totalGst / 2.0
            GstResult(
                baseAmount = base,
                gstPercent = gstRate,
                cgstAmount = halfGst,
                sgstAmount = halfGst,
                totalGstAmount = totalGst,
                grossAmount = amount
            )
        }
    }
}
