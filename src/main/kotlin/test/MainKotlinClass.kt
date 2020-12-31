package test

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import java.io.FileNotFoundException
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat

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
        fun test() {
            print("test")
        }

        @JvmStatic
        public fun main(args: Array<String>) {
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
            var prowizja = 0.39f
            if (!prowizjaIn.isEmpty())
                prowizja = prowizjaIn.replace(",", ".").toFloat()
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
                            if (res.size != 9 || res[0].contains("czas", true))
                                return@let

                            val transaction = Transaction().apply {
                                println("test")
                                println(res[0])

                                try {
                                    data = SimpleDateFormat("dd.MM.yyyy").parse(res[0])

                                    walor = res[1]
                                    gielda = res[2]
                                    rodzaj = res[3]
                                    liczba = res[4].replace("\\s".toRegex(), "").toInt()
                                    kurs = res[5].replace("\\s".toRegex(), "")
                                        .replace(",", ".").toFloat()
                                    walutaKurs = res[6]
                                    wartosc = res[7].replace("\\s".toRegex(), "")
                                        .replace(",", ".").toFloat()
                                    walutaWartosc = res[8]

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

        private fun calcFinalRes(transactions: ArrayList<Transaction>, rokFilter: String, prowizja: Float): String {
            // unikalne walory
            val walory = getUniqueWalory(transactions)

            var totalBought = 0f
            var totalSold = 0f
            val resList = ArrayList<StockResult>()

            //liczenie transakcji calosciowych dla walorow
            for (walor in walory) {
                val buyTrans =
                    ArrayList<Transaction>().apply { addAll(transactions.filter { it.walor == walor && it.rodzaj == "K" }) }
                val sellTrans = transactions.filter { it.walor == walor && it.rodzaj == "S" }

                var totalTansBought = 0f
                var totalTransSold = 0f

                for (sellTran in sellTrans) {
                    val liczba = sellTran.liczba
                    val buyValue = getBuyTransValueAndProcess(buyTrans, liczba)

                    if (rokFilter.isNotEmpty()) {
                        val year = sellTran.data.year + 1900
                        if (year.toString() != rokFilter && sellTran.rodzaj == "S")
                            continue
                    }

                    totalTansBought += buyValue + (buyValue * prowizja / 100)
                    totalTransSold += sellTran.wartosc - (sellTran.wartosc * prowizja / 100)

                    totalBought += buyValue + (buyValue * prowizja / 100)
                    totalSold += sellTran.wartosc - (sellTran.wartosc * prowizja / 100)
//                println("SPRZEDANO ${sellTran.liczba} za ${sellTran.wartosc} KUPIONYCH ZA ${buyValue} ")
                }

                val diff = totalTransSold - totalTansBought
                val percentage = totalTransSold * 100 / totalTansBought - 100

                val nextPrice = if (buyTrans.size > 0) buyTrans[0].kurs else 0f

                resList.add(StockResult().apply {
                    this.walor = walor
                    this.res = diff
                    perc = percentage
                    this.nextPrice = nextPrice
                })
//            println("$walor \t\t $diff \t $percentage %")
            }

            val diff = totalSold - totalBought
            val percentage = totalSold * 100 / totalBought - 100

            return printFinalRes(resList, diff, percentage)
        }

        private fun printFinalRes(resList: ArrayList<StockResult>, totalDiff: Float, totalPercentage: Float): String {
            var resString = ""
            println(
                ANSI_BLUE +
                        String.format("%15s %15s %15s %15s", "WALOR", "ZYSK", "PROCENT", "N. SPRZEDAZ")
                        + ANSI_RESET
            )


            resString += String.format(
                getWindowStrongFormat(),
                "WALOR", "ZYSK", "PROCENT", "N. SPRZEDAZ"
            ) + "\n"
            resString = addSeparator(resString) + "\n"

            resList.sortBy { it.res }
            resList.reverse()

            var separatorAdded = false

            for (stockResult in resList) {
                if (stockResult.res == 0f)
                    continue
                val diffString = String.format("%.2f", stockResult.res)
                val percentageString = String.format("%.2f", stockResult.perc) + " %"
                var nextPrice = String.format("%.2f", stockResult.nextPrice)
                if (stockResult.nextPrice == 0f)
                    nextPrice = "-"


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
                                "%15s %15s %15s %15s",
                                stockResult.walor,
                                diffString,
                                percentageString,
                                nextPrice
                            )
                            + ANSI_RESET
                )


                resString += String.format(
                    getWindowStrongFormat(),
                    stockResult.walor + " |",
                    diffString,
                    percentageString,
                    nextPrice
                ) + "\n"
            }

            resString += "\n"
            println()
            var color = ANSI_WHITE
            if (totalDiff > 1)
                color = ANSI_GREEN
            if (totalDiff < -1)
                color = ANSI_RED
            println(color + "WYNIK CALKOWITY = $totalDiff : $totalPercentage %" + ANSI_RESET)

            resString = addSeparator(resString)
            resString += String.format(
                getWindowStrongFormat(),
                "WYNIK CALKOWITY",
                totalDiff,
                "$totalPercentage %", ""
            )

            return resString
        }

        private fun getWindowStrongFormat(): String {
            val leng1 = 30
            val leng2 = 20
            val leng3 = 20
            val leng4 = 20
            return "%" + leng1 + "s %" + leng2 + "s %" + leng3 + "s %" + leng4 + "s"
        }

        private fun addSeparator(resString: String): String {
            var resString1 = resString
            resString1 += String.format(
                getWindowStrongFormat(),
                "-----------",
                "-----------",
                "-----------",
                "-----------"
            ) + "\n"
            return resString1
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
        fun getBuyTransValueAndProcess(buyTrans: ArrayList<Transaction>, liczba: Int): Float {
            if (buyTrans.size == 0)
                return 0f

            var res: Float
            val buyTran = buyTrans[0]
            if (liczba <= buyTran.liczba) {
                buyTran.liczba -= liczba
                res = buyTran.kurs * liczba
                if (buyTran.liczba == 0)
                    buyTrans.removeAt(0)
            } else {
                res = buyTran.kurs * buyTran.liczba
                buyTrans.removeAt(0)
                res += getBuyTransValueAndProcess(buyTrans, liczba - buyTran.liczba)
            }

            return res
        }
    }


}
