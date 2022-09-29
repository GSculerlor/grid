import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.min

/*
@LayoutScopeMarker
interface BSPLScope {
    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        itemContent: @Composable BSPLScope.(index: Int) -> Unit
    )
}
*/

@Composable
fun FernBSPLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val childCount = measurables.size

        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    minHeight = min(constraints.minHeight, constraints.maxHeight),
                    minWidth = min(constraints.minWidth, constraints.maxWidth / childCount),
                    maxHeight = constraints.maxHeight,
                    maxWidth = constraints.maxWidth / childCount
                )
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var xPos = 0

            placeables.forEach {
                it.placeRelative(xPos, 0)
                xPos += it.width
            }
        }
    }
}