import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun StartButton(onClick: () -> Unit, modifier: Modifier = Modifier)
