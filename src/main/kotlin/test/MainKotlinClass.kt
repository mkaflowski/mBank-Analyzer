package test

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import java.io.FileNotFoundException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.math.BigDecimal

import java.util.Locale

import java.text.DecimalFormatSymbols

import java.text.DecimalFormat
import kotlin.math.min


const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001b[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"


class MainKotlinClass {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(ANSI_CYAN + "Witaj w programie eMakler Analyser!" + ANSI_RESET)
            println(ANSI_YELLOW + "Podaj ścieżkę pliku csv: " + ANSI_RESET)
            var path = readLine()!!.trim()
            if (path.isEmpty())
                path = "/Users/mateuszkaflowski/Downloads/maklerfile.Csv"

            println(ANSI_YELLOW + "Giełda [GPW,NASDAQ] - lub zostaw puste (domyślnie wszystkie): " + ANSI_RESET)
            val gieldaFilter = readLine()!!

            println(ANSI_YELLOW + "Rok [2019,2020] - lub zostaw puste (domyślnie wszystkie): " + ANSI_RESET)
            val rokFilter = readLine()!!

            println(ANSI_YELLOW + "Prowizja od transakcji (domyślnie 0,39%): " + ANSI_RESET)
            var prowizjaIn = readLine()!!

            calc(path, gieldaFilter, rokFilter, prowizjaIn)
        }

        @JvmStatic
        fun calc(path: String, gieldaFilter: String, rokFilter: String, prowizjaIn: String): String {
            var prowizja = 0.39
            if (!prowizjaIn.isEmpty())
                prowizja = prowizjaIn.replace(",", ".").toDouble()
            val transactions = ArrayList<Transaction>()
            val context = CsvReaderContext().apply {
                charset = Charsets.ISO_8859_1.name()
                quoteChar = '\''
                delimiter = ';'
                escapeChar = '"'
                skipEmptyLine = true
            }
            val reader = CsvReader(context)

            //"/Users/mateuszkaflowski/Downloads/maklerfile.Csv"
            try {
                reader.open(path) {
                    var res: List<String>? = null
                    do {
                        res = readNext()

                        res?.let {
                            if ((res.size != 9 && res.size != 11) || res[0].contains("czas", true))
                                return@let

                            val transaction = Transaction().apply {
//                                println(res[0])

                                try {
                                    data = SimpleDateFormat("dd.MM.yyyy").parse(res[0])

                                    walor = res[1]
                                    gielda = res[2]
                                    rodzaj = res[3]
//                                    if (rodzaj == "K")
                                        walor = removeIpoNameAndPDA(walor, transactions)

                                    liczba = res[4].replace("\\s".toRegex(), "").toInt()
                                    kurs = res[5].replace("\\s".toRegex(), "")
                                        .replace(",", ".").toDouble()
                                    walutaKurs = res[6]

                                    //stara wersja
                                    if (res.size == 9) {
                                        wartosc = res[7].replace("\\s".toRegex(), "")
                                            .replace(",", ".").toDouble()
                                        walutaWartosc = res[8]
                                    }
                                    //nowa wersja
                                    if (res.size == 11) {
                                        wartosc = res[9].replace("\\s".toRegex(), "")
                                            .replace(",", ".").toDouble()
                                        walutaWartosc = res[10]
                                    }

                                    if (walutaKurs != walutaWartosc) {
                                        walutaKurs = walutaWartosc

                                        kurs = wartosc / liczba
                                    }
                                } catch (e: Exception) {
                                    println("Parsing problem")
//                                    e.printStackTrace()
                                    return@let
                                }
                            }

                            if (gieldaFilter.isNotEmpty())
                                if (!transaction.gielda.contains(gieldaFilter, true))
                                    return@let

                            transactions.add(transaction)
                            //                println(res)
                        }
                    } while (res != null)

                    transactions.reverse()
                    transactions.sortBy { it.data }

                    println("WSZYSTKICH TRANSAKCJI - ${transactions.size}")
                    println("-------------------")

                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return "NIE ZNALEZIONO PLIKU"
            }

            return calcFinalRes(transactions, rokFilter, prowizja)
        }

        private fun removeIpoNameAndPDA(walor: String, transactions: ArrayList<Transaction>): String {
            if (walor.contains("-IPO") || walor.contains("GROU-PDA") ) {
                val res = transactions.find { transaction ->
                    transaction.walor.contains(
                        walor.subSequence(0, min(2, walor.length))
                    )
                }
                res?.let {
                    println("ZNALEZIONO IPO: $walor -> ${it.walor}")
                    return it.walor
                }

            }
            return walor
        }

        private fun calcFinalRes(transactions: ArrayList<Transaction>, rokFilter: String, prowizja: Double): String {
            var res = ""

            // unikalne walory
            val walory = getUniqueWalory(transactions)

            var totalBought = 0.0
            var totalSold = 0.0
            val resList = ArrayList<StockResult>()

            //liczenie transakcji calosciowych dla walorow
            walor@ for (walor in walory) {
                val buyTrans =
                    ArrayList<Transaction>().apply { addAll(transactions.filter { it.walor == walor && it.rodzaj == "K" }) }
                val sellTrans = transactions.filter { it.walor == walor && it.rodzaj == "S" }

                var totalTansBought = 0.0
                var totalTransSold = 0.0

                var totalAkcjeSold = 0

                for (sellTran in sellTrans) {

                    val liczba = sellTran.liczba
//                    println("$walor $liczba ${sellTran.data}")

                    try {
                        val buyValue = getBuyTransValueAndProcess(buyTrans, sellTran)
                        if (rokFilter.isNotEmpty()) {
                            val year = sellTran.data.year + 1900
                            if (year.toString() != rokFilter && sellTran.rodzaj == "S")
                                continue
                        }

                        totalAkcjeSold += liczba

                        totalTansBought += buyValue + (buyValue * prowizja / 100)
                        totalTransSold += sellTran.wartosc - (sellTran.wartosc * prowizja / 100)

                        totalBought += buyValue + (buyValue * prowizja / 100)
                        totalSold += sellTran.wartosc - (sellTran.wartosc * prowizja / 100)

//                    println("$walor SPRZEDANO ${sellTran.liczba} za ${sellTran.wartosc} KUPIONYCH ZA ${buyValue} ")
                    } catch (e: NegativeArraySizeException) {
                        res += "\t$walor \tBŁĘDNA LICZBA AKCJI - WIĘCEJ SPRZEDANYCH NIŻ KUPINYCH\n"
                        continue@walor
                    }
                }

                val diff = totalTransSold - totalTansBought
                val percentage = totalTransSold * 100 / totalTansBought - 100

                val nextPrice = if (buyTrans.size > 0) buyTrans[0].kurs else 0.0

                resList.add(StockResult().apply {
                    this.walor = walor
                    this.res = diff
                    perc = percentage
                    this.nextPrice = nextPrice
                    this.avrageSold = totalTransSold / totalAkcjeSold
                    this.avrageBought = totalTansBought / totalAkcjeSold
                })
//            println("$walor \t\t $diff \t $percentage  %")
            }

            val diff = totalSold - totalBought
            val percentage = totalSold * 100 / totalBought - 100

            res += "\n" + printFinalRes(resList, diff, percentage)
            return res
        }

        private fun printFinalRes(resList: ArrayList<StockResult>, totalDiff: Double, totalPercentage: Double): String {
            var resString = ""
            println(
                ANSI_BLUE +
                        String.format("%15s %15s %15s %35s", "WALOR", "ZYSK", "PROCENT", "ŚR. K/S (NASTĘPNA S)")
                        + ANSI_RESET
            )

            resString += String.format(
                getWindowStrongFormat(),
                "WALOR", "ZYSK", "PROCENT", "ŚR. K/S (NASTĘPNA S)"
            ) + "\n"
            resString = addSeparator(resString) + "\n"

            resList.sortBy { it.res }
            resList.reverse()

            var separatorAdded = false

            for (stockResult in resList) {
                if (stockResult.res == 0.0)
                    continue
                val diffString = getFormattedNumber(stockResult.res)
                val percentageString = String.format("%.2f", stockResult.perc) + " %"
                var nextPrice = String.format("%.2f", stockResult.nextPrice)
                if (stockResult.nextPrice == 0.0)
                    nextPrice = "-"

                val avrageBought = getFormattedNumber(stockResult.avrageBought)
                val avrageSold = getFormattedNumber(stockResult.avrageSold)

                var color = ANSI_WHITE
                if (stockResult.perc > 1)
                    color = ANSI_GREEN
                if (stockResult.perc < -1)
                    color = ANSI_RED

                if (!separatorAdded && stockResult.res < 0) {
                    separatorAdded = true
                    resString = addSeparator(resString)
                }

                println(
                    color +
                            String.format(
                                "%15s %15s %15s %35s",
                                stockResult.walor,
                                diffString,
                                percentageString,
                                "$avrageBought / $avrageSold ($nextPrice)"
                            )
                            + ANSI_RESET
                )

                resString += String.format(
                    getWindowStrongFormat(),
                    stockResult.walor + " |",
                    diffString,
                    percentageString,
                    "$avrageBought / $avrageSold ($nextPrice)"
                ) + "\n"
            }

            resString += "\n"
            println()
            var color = ANSI_WHITE
            if (totalDiff > 1)
                color = ANSI_GREEN
            if (totalDiff < -1)
                color = ANSI_RED


            println(color + "WYNIK CALKOWITY = ${getFormattedNumber(totalDiff)} : $totalPercentage %" + ANSI_RESET)

            resString = addSeparator(resString)
            resString += String.format(
                getWindowStrongFormat(),
                "WYNIK CALKOWITY",
                getFormattedNumber(totalDiff),
                "${getFormattedNumber(totalPercentage)} %", ""
            )

            return resString
        }

        private fun getFormattedNumber(d: Double): String {
            val df = DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols(Locale("pt", "BR"))
            )

            val value = BigDecimal(d)

            return df.format(value.toFloat().toDouble())
        }

        private fun getWindowStrongFormat(): String {
            val leng1 = 30
            val leng2 = 20
            val leng3 = 20
            val leng4 = 35
            return "%" + leng1 + "s %" + leng2 + "s %" + leng3 + "s %" + leng4 + "s"
        }

        private fun addSeparator(resString: String): String {
            var resString1 = resString
            resString1 += getSeparator()
            return resString1
        }

        @JvmStatic
        fun getSeparator(): String {
            return String.format(
                getWindowStrongFormat(),
                "-----------",
                "-----------",
                "-----------",
                "-----------"
            ) + "\n"
        }

        private fun getUniqueWalory(transactions: ArrayList<Transaction>): ArrayList<String> {
            val walory = ArrayList<String>().apply {
                val tmp = transactions.distinctBy { it.walor }
                println("WSZYSTKICH WALORÓW - ${tmp.size} :")
                for (t in tmp) {
                    add(t.walor)
                }
            }
            return walory
        }

        /**
         * Zwraca wartość akcji w momencie zakupu i usuwa je z historii
         */
        fun getBuyTransValueAndProcess(buyTrans: ArrayList<Transaction>, sellTran: Transaction): Double {
            if (buyTrans.size == 0 && sellTran.liczba > 0) {
                println("${sellTran.walor} - BŁĘDNA LICZBA AKCJI - WIĘCEJ SPRZEDANYCH NIŻ KUPINYCH")
//                return 0.0
                throw NegativeArraySizeException()
            }
            if (buyTrans.size == 0)
                return 0.0

            var res: Double
            val buyTran = buyTrans[0]

            if (buyTran.data.after(sellTran.data)) {
                println("${sellTran.walor} ${sellTran.data} - SPRZEDAŻ PRZED ZAKUPEM? - UZNAJĘ SPLIT")
                return 0.0
            }

            if (sellTran.liczba <= buyTran.liczba) {
                buyTran.liczba -= sellTran.liczba
                res = buyTran.kurs * sellTran.liczba

//                println("${buyTran.walor} ${buyTran.kurs} ${sellTran.kurs} - SPRZEDAŻ")

                if (buyTran.liczba == 0)
                    buyTrans.removeAt(0)
            } else {
                res = buyTran.kurs * buyTran.liczba
                buyTrans.removeAt(0)
                sellTran.liczba -= buyTran.liczba

//                println("${buyTran.walor} ${buyTran.kurs} ${sellTran.kurs} - SPRZEDAŻ")

                res += getBuyTransValueAndProcess(buyTrans, sellTran)
            }

            return res
        }
    }


}
