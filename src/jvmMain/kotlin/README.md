# compose code styles/rules v2: electric boogaloo

## composable function

### composable function naming convention

You must name any function that returns `Unit` and bears the `@Composable` annotation using PascalCase. This
guideline applies whether the function emits UI elements or not.

#### do

```kotlin
@Composable
fun FernFlowContainer(modifier: Modifier = Modifier) {
    // something here
}
```

#### don't

```kotlin
@Composable
fun fernFlowContainer(modifier: Modifier = Modifier) {
    // something here
}
```

You must follow the standard Kotlin Coding Conventions for the naming of functions for any function
annotated `@Composable` that returns a value other than `Unit`.

#### do

```kotlin
@Composable
fun shimmerBrush(): Brush {
    // something here
}
```

#### don't

```kotlin
@Composable
fun ShimmerBrush(): Brush {
    // something here
}
```

useful links:
[Naming Unit @Composable functions as entities](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md#naming-unit-composable-functions-as-entities)
and [Naming @Composable functions that return values](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md#naming-composable-functions-that-return-values)

---

### don't emit multiple layout node on a single composable function

A composable function should emit either 0 or 1 pieces of layout, but no more. A composable function should be cohesive,
and not rely on what function it is called from.

Says you want to split your `Column` content to a separated composable function like in the example below.

```kotlin
@Composable
private fun SectionWithTitle(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        SectionWithTitleContent(title, content)
    }
}

@Composable
private fun SectionWithTitleContent(title: String, content: @Composable () -> Unit) {
    Text(text = title)
    content()
}
```

While this code looks correct in a sense, there's nothing to stop `SectionWithTitleContent` to be called by other
composable function other than `Column`. For example, if you called it inside a `Row` or `Box` it will behave
differently. You MUST make it cohesive and emit a single layout node.

```kotlin
@Composable
private fun SectionWithTitle(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Text(text = title, modifier = Modifier.weight(0.5f))
        content()
    }
}
```

So what should you do if you really want to split the content? For the example above, since you emit them
inside `Column`, you can create an extension function for `ColumnScope`.

```kotlin
@Composable
private fun SectionWithTitle(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        SectionWithTitleContent(title, content)
    }
}

@Composable
private fun ColumnScope.SectionWithTitleContent(title: String, content: @Composable () -> Unit) {
    Text(text = title, modifier = Modifier.weight(0.5f))
    content()
}
```

---

### don't emit AND return something on a single composable function

Composable functions should either emit layout content, or return a value, but not both. I mean, why would you do that
in the first place ¯\_(ツ)_/¯.

#### don't

```kotlin
@Composable
private fun RandomText(seed: Int, modifier: Modifier = Modifier): String {
    val randomText = randomTextGenerator(seed)
    Text(text = "generated: $randomText")

    return randomText
}
```

useful link:
[Emit XOR return a value](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md#emit-xor-return-a-value)

---

### don't pass mutable types as a parameter

Avoid passing mutable types (`ArrayList<T>`, `MutableState<T>`, `MutableList<T>`, etc.) as composable function
parameters. This is to avoid data stale since mutable types or objects does not trigger recomposition and making your
compose layout node doesn't reflect the latest value of your state. If your composable function might change value of
your state, expose lambda function as a callback instead.

#### do

```kotlin
@Composable
fun MagicButton(list: List<Int>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column {
        Text(text = list.joinToString(separator = ","), color = Color.White)
        Button(onClick = onClick) {
            Text(text = "Click ME!")
        }
    }
}
```

#### don't

```kotlin
@Composable
fun MagicButton(list: MutableList<Int>, modifier: Modifier = Modifier) {
    Column {
        Text(text = list.joinToString(separator = ","), color = Color.White)
        Button(onClick = { list.add(1) }) {
            Text(text = "Click ME!")
        }
    }
}
```

useful
link: [Jetpack Compose Stability Explained](https://medium.com/androiddevelopers/jetpack-compose-stability-explained-79c10db270c8)

---

### composable function parameter order

When you create a composable function with multiple parameters, make sure you follow this order:

1. params without defaults
2. modifier
3. params with defaults
4. optional: function that might have no default, something like trailing function

```kotlin
@Composable
fun Button(
    onClick: () -> Unit, // param without defaults
    modifier: Modifier = Modifier, // modifier
    enabled: Boolean = true, // param with default
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit // trailing function
)
```

useful links:
[Elements accept and respect a Modifier parameter](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md#elements-accept-and-respect-a-modifier-parameter)
and [Modifier doc](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier)

---

### make composable function as pure as possible

Try to avoid any side effect on your composable function that can lead to any instablity of the composable function
itself. If you need to do something that the scope is outside the lifecycle of the composable function (viewmodel
action, network API call, state-based action), use compose side effect APIs like `SideEffect`, `DisposableEffect`
, `LaunchedEffect`, etc.

useful link: [Side-effects in Compose](https://developer.android.com/jetpack/compose/side-effects)

---

### don't access or even modifying global variable inside composable function

Since composable function can run in any order, try to avoid accessing and modifying global variable since it can also
make your composable function not idempotent.

#### don't

```kotlin
var data = 0

@Composable
fun SomeScreen(modifier: Modifier = Modifier) {
    Column {
        Text(text = data)
        Button(onClick = { data++ }) { Text(text = "+1") }
        Text(text = data)
    }
}
```

useful link:[Thinking of Compose](https://developer.android.com/jetpack/compose/mental-model)

---

## modifier

### modifier order/chain matters

Modifier elements may be combined using then. Order is significant; modifier elements that appear first will be applied
first.
If you really hard to follow this concept, try copy-paste this two composable function and see the result.

```kotlin
@Composable
fun FirstBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Red)
            .padding(8.dp)
            .size(32.dp)
            .border(1.dp, Color.Blue)
    )
}

@Composable
fun SecondBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Red)
            .size(32.dp)
            .padding(8.dp)
            .border(1.dp, Color.Blue)
    )
}
```

useful link: [Modifier doc](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier)

---

### don't reuse modifier

Modifiers are designed so that they should be used by a single layout node in the composable function (most of the time
is the root composable function). Things might brake if you reuse the modifier.

#### do

```kotlin
@Composable
fun FernLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}
```

#### don't

```kotlin
@Composable
fun FernLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        CircularProgressIndicator(modifier = modifier.align(Alignment.Center))
    }
}
```

---

### always provide modifier (and provide default `Modifier` parameter)

Modifiers are the standard means of adding external behavior to an element in Compose UI and allow common behavior to be
factored out of individual or base element API surfaces. This allows element APIs to be smaller and more focused, as
modifiers are used to decorate those elements with standard behavior. Also, as discussed in the previous section,
composables that accept a Modifier as a parameter to be applied to the whole component represented by the composable
function should name the parameter modifier and assign the parameter a default value of Modifier. It should appear as
the first optional parameter in the parameter list.

But, not every composable function should provide modifier. That exception is applied to:

- doesn't emit content
- part of an interface
- a `@Preview` composable

useful links:
[Always provide a Modifier parameter](https://chris.banes.dev/always-provide-a-modifier/)
and [Modifier doc](https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier)

---

## preview

### use `@PreviewParameter` when you're dealing with multiple cases

You can use `@PreviewParameter` to provide sample data for your composables, instead of doing it all in a separated
preview or using list layout like `Row` or `Column`.

#### do

```kotlin
class FernChapterItemDataPreview :
    CollectionPreviewParameterProvider<FernChapterItemDataPreview.ChapterItem>(
        listOf(
            ChapterItem("Ch. 1 - Denial", "Scanlator"),
            ChapterItem("Ch. 2 - Anger", "Different Scanlator"),
            ChapterItem("Ch. 3 - Bargaining", "Different Scanlator"),
        )
    ) {
    data class ChapterItem(
        val title: String,
        val scanlator: String
    )
}

@Preview(showBackground = true)
@Composable
private fun FernChapterItemPreview(@PreviewParameter(FernChapterItemDataPreview::class) param: FernChapterItemDataPreview.ChapterItem) {
    FernTheme {
        FernChapterItem(
            title = param.title,
            scanlator = param.scanlator
        )
    }
}
```

#### don't

```kotlin
@Preview(showBackground = true)
@Composable
private fun FernChapterItemPreview() {
    FernTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FernChapterItem(
                title = "Ch. 1 - Denial",
                scanlator = "Scanlator"
            )
            FernChapterItem(
                title = "Ch. 2 - Anger",
                scanlator = "Different Scanlator"
            )
            FernChapterItem(
                title = "Ch. 3 - Bargaining",
                scanlator = "Different Scanlator"
            )
        }
    }
}
```

---

### use `private` for preview composable function

Composable function that annotated with `@Preview` doesn't need to have public visibility because it won't be used and
shouldn't be used anywhere.

```kotlin
@Preview
@Composable
private fun FernChipPreview() {
    FernTheme {
        FernChip(label = "fern", onClick = { })
    }
}
```

---

## state hoisting

### hoist every state whenever possible

Compose is built upon the idea of a unidirectional data flow, which can be summarised as: data/state flows down, and
events fire up. To implement that, Compose advocates for the pattern of hoisting state upwards, enabling the majority of
your composable functions to be stateless. This has many benefits, including far easier testing.

In practice, there are a few common things to look out for:

- Do not pass ViewModels (or objects from DI) down.
- Do not pass MutableState<T> instances down.
- Instead pass down the relevant data to the function, and optional lambdas for callbacks.

Another thing to take a note when creating a state is make sure you wrap your state in a `remember` block, especially
when the state is not hoisted in our view state that exposed by viewmodel. This will make sure that your state isn't
recreated when your composable function is recomposed.

#### do

```kotlin
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val viewState: HomeViewState by viewModel.viewState.collectAsState()
    HomeScreen(modifier = modifier, viewState = viewState)
}

@Composable
private fun HomeScreen(viewState: HomeScreenViewState, modifier: Modifier = Modifier) {
    // something here
}

```

#### don't

```kotlin
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreen(modifier = modifier, viewModel = viewModel)
}

@Composable
private fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    // something here that using viewModel
}
```

Another example is when creating a custom widget that hold state. Instead of your widget doing the state holding by
handling the remembering things, making it a stateful composable function. let your widget composable function expose
the value and the on change callback instead.

#### do

```kotlin
@Composable
private fun SomeScreen(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf("") }

    Surface {
        SomeWidget(state = state, onChangeState = { state = it })
    }
}

@Composable
fun SomeWidget(
    state: String,
    modifier: Modifier = Modifier,
    onChangeState: (String) -> Unit,
) {
    Column {
        Text(text = state)
        Button(
            onClick = {
                val newState = "" // do some magic here
                onChangeState(newState)
            }
        ) {
            Text(text = "Update State")
        }
    }
}
```

#### don't

```kotlin
@Composable
private fun SomeScreen(modifier: Modifier = Modifier) {
    Surface {
        SomeWidget()
    }
}

@Composable
fun SomeWidget(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf("") }

    Column {
        Text(text = state)
        Button(
            onClick = {
                val newState = "" // do some magic here
                state = newState
            }
        ) {
            Text(text = "Update State")
        }
    }
}
```

---

### stability of the state matters

Compose compiler tries to infer immutability and stability on value classes, but sometimes it gets it wrong, which then
means that your UI will be doing more work than it needs. Try to avoid creating state that considered as a non-stable
object by the compiler.

### do

```kotlin
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val viewState: HomeViewState by viewModel.viewState.collectAsState()

    HomeScreen(
        modifier = modifier,
        viewState = viewState,
        onUpdateState = viewModel::onUpdateState
    )
}
```

#### don't

```kotlin
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // this might confuse the compiler as you can't guarantee its stability
    val onUpdateState = { viewModel.onUpdateState(state) }
    val viewState: HomeViewState by viewModel.viewState.collectAsState()

    HomeScreen(
        modifier = modifier,
        viewState = viewState,
        onUpdateState = onUpdateState
    )
}
```

If you know that your object/class is a stable or immutable, you can mark your object with `@Immutable` or `@Stable`

```kotlin
@Immutable
data class ChapterItem(
    val title: String,
    val scanlator: String
)
```

useful links:
[Immutable source code](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/Immutable.kt)
and [Stable source code](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/Stable.kt;l=49?q=Stable&ss=androidx%2Fplatform%2Fframeworks%2Fsupport)
---

## viewmodel (or any injected DI object)

Viewmodels are typically passed to composable function as a param with default value (if you're using Hilt
by `hiltViewModel`) or by get instance inside the composable function (like `getViewModel<T>` if you use Koin
or `rememberInstance<T>` if you use Kodein). Now since composable have different lifecycle as traditional View or
Fragment or Activity, you need to make sure that you get the instance in a composable function that don't get recomposed
when something changed. Take a look at the example below.

```kotlin
@Composable
fun SomeDIScreen(title: String, modifier: Modifier = Modifier, onUpdateTitle: (String) -> Unit) {
    val viewModel: ScreenViewModel by rememberInstance<ScreenViewModel>()
    val viewState by viewModel.viewState.collectAsState()

    Column(modifier = modifier) {
        Text(text = title)
        SomeStatefulLayout(state = viewState)
        Button(onClick = { onUpdateTitle("ayaya") }) {
            Text(text = "Update Title")
        }
    }
}
```

Now everytime title changed, `SomeDIScreen` will be recomposed and reinvoked again, and since `rememberInstance` will
provide a new instance everytime it gets called, something that we don't want to happen since it will reset the
viewstate too. One solution for this issue is by asking the DI to provide same object whenever we need
by `rememberSingleton`. But it has a downside that now your viewmodel will reside in your memory until you close the
app, not the screen. Another solution (and the preferable one) is by hoist the viewmodel to the top function, a
composable function that don't get recomposed whenever you change something. Take a look at the example below.

```kotlin
@Composable
fun SomeDIRoute(modifier: Modifier = Modifier, onUpdateTitle: (String) -> Unit) {
    val viewModel: ScreenViewModel by rememberInstance<ScreenViewModel>()
    val viewState by viewModel.viewState.collectAsState()

    SomeDIScreen(modifier = modifier, title = "title", viewState = viewState, onUpdateTitle = onUpdateTitle)
}

@Composable
private fun SomeDIScreen(
    title: String,
    viewState: ScreenViewState,
    modifier: Modifier = Modifier,
    onUpdateTitle: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = title)
        SomeStatefulLayout(state = viewState)
        Button(onClick = { onUpdateTitle("ayaya") }) {
            Text(text = "Update Title")
        }
    }
}
```

Now by hoisting the viewmodel on composable function that doesn't get recomposed, viewmodel won't be recreated everytime
state changed, saving your time from debugging "why the hell my state is gone or flushed" moment.

---

## navigation

our navigation is kinda lul.

---

## debugging your compose function

If you're trying to check your compose layout tree attributes or you face some issue and unsure whether your composable
function doing unnecessary recomposition or not, you can use Android Studio's Layout Inspector. Make sure you enable
"Enable view attribute inspection" setting on your device.

![Layout Inspector example](https://gsculerlor.s-ul.eu/R5X4Y1jB)

useful link:
[Jetpack Compose: Debugging Recomposition](https://medium.com/androiddevelopers/jetpack-compose-debugging-recomposition-bfcf4a6f8d37)
