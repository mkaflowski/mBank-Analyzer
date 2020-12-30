package test

import javafx.geometry.Orientation
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Stage
import tornadofx.*


class MainView : View() {
    companion object {
        @JvmStatic
        public fun main(args: Array<String>){
            launch<TornadoApp>(args)
        }
    }

    private val function = {
        field("Plik csv") { textfield() }
        field("Giełda [GPW,NASDAQ] (puste = wszystkie)") { textfield() }
        field("Rok (puste = wszystkie)") { textfield() }
        field("Prowizja") { textfield("0.39") }
        val button = button("LICZ") {
            style = "-fx-base: #57b757;"
            action {
                println("Handle button press")
            }
        }
    }

    private lateinit var resArea: TextArea
    private lateinit var pathField: TextField
    private lateinit var rokField: TextField
    private lateinit var gieldaField: TextField
    private lateinit var prowizjaField: TextField

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            setOnDragDropped { event ->
                println(event.dragboard.files.first().absolutePath)
                pathField.text = event.dragboard.files.first().absolutePath
            }

            field("Plik csv") {
                pathField = textfield("/Users/mateuszkaflowski/Downloads/transakcje.Csv") {
                    setOnDragEntered { event ->
                        println(event.dragboard.files.first().absolutePath)
                        pathField.text = event.dragboard.files.first().absolutePath
                    }
                }
            }
            field("Giełda [GPW,NASDAQ] (puste = wszystkie)") { gieldaField = textfield() }
            field("Rok (puste = wszystkie)") { rokField = textfield() }
            field("Prowizja") { prowizjaField = textfield("0.39") }
            button("LICZ") {
                style = "-fx-base: #57b757;"
                action {
                    val resString = MainKotlinClass.calc(
                        pathField.text.trim(), gieldaField.text,
                        rokField.text, prowizjaField.text
                    )
                    resArea.text = resString
                }
            }

            resArea = textarea {
                style =
                    "-fx-control-inner-background:#000000;-fx-font-family: Consolas; -fx-highlight-fill: #00ff00;" +
                            "-fx-highlight-text-fill: #000000; -fx-text-fill: #ffffff; "
                id = "result"
                prefRowCount = 20
                vgrow = Priority.ALWAYS
            }
        }
    }
}

class TornadoApp : App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minHeight = 200.0
        stage.minWidth = 800.0

        super.start(stage)
    }

    override fun createPrimaryScene(view: UIComponent) = super.createPrimaryScene(view).apply {
        fill = Color.valueOf("#EDEDED")
    }
}
