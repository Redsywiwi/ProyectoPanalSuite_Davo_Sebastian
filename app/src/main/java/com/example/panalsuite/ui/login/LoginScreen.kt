package com.example.panalsuite.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.ui.components.ScreenBackground
import com.example.panalsuite.ui.theme.PanalSuiteTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.*
import com.example.panalsuite.model.User
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import android.os.Build
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import android.media.MediaPlayer
private val FieldShape = RoundedCornerShape(16.dp)
private val CardShape = RoundedCornerShape(28.dp)

// Las animaciones gemelo, en milisegundos


// https://developer.android.com/develop/ui/compose/animation/composables-modifiers?hl=es-419
// LAS ANIMACIONES
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    errorMessage: String? = null,
    testUsers: List<User> = emptyList(),
    testUsersError: String? = null,
    modifier: Modifier = Modifier
){
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isContentVisible by remember { mutableStateOf(false) }
    var showTestMenu by rememberSaveable { mutableStateOf(false) }
    var showForgotPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { isContentVisible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenBackground(imageRes = R.drawable.freljord)

        if (showTestMenu) {
            TestUsersDialog(
                users = testUsers,
                error = testUsersError,
                onUserSelected = { user ->
                    email = user.email
                    password = user.password
                    showTestMenu = false
                },
                onDismiss = { showTestMenu = false }
            )
        }
        if (showForgotPassword) {
            ForgotPasswordDialog(onDismiss = { showForgotPassword = false })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DelayedEntrance(isVisible = isContentVisible, order = 0) {
                LoginHeader(onLogoClick = { showTestMenu = true })
            }

            Spacer(modifier = Modifier.height(32.dp))

            DelayedEntrance(isVisible = isContentVisible, order = 1) {
                LoginCard(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    isPasswordVisible = isPasswordVisible,
                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                    onLoginClick = { onLoginClick(email, password) },
                    onForgotPasswordClick = { showForgotPassword = true },
                    errorMessage = errorMessage
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}

@Composable
private fun DelayedEntrance(
    isVisible: Boolean,
    order: Int,
    content: @Composable () -> Unit
) {
    val delayMillis = order * 150

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500, delayMillis)) +
                slideInVertically(
                    animationSpec = tween(500, delayMillis, FastOutSlowInEasing)
                )
    ) {
        content()
    }
}


@Composable
private fun LoginHeader(onLogoClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Logo(onClick = onLogoClick)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

    }
}

@Composable
private fun Logo(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_bzzt),
            contentDescription = null,
            modifier = Modifier.size(70.dp)
        )
    }
}
@Composable
private fun TestUsersDialog(
    users: List<User>,
    error: String?,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Rounded.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(text = stringResource(R.string.debug_menu_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (users.isEmpty()) {
                    Text(
                        text = error ?: "No se cargo ningun usuario (users.json vacio o no encontrado)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                users.forEach { user ->
                    TextButton(
                        onClick = { onUserSelected(user) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "#${user.idUser} | ${user.role} | ${user.name}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.debug_menu_close))
            }
        }
    )
}

@Composable
private fun ForgotPasswordDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.risa).apply {
            start()
        }

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
            }
            .build()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            AsyncImage(
                model = R.drawable.gato,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.debug_menu_close))
            }
        }
    )
}

@Composable
private fun LoginCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    errorMessage: String? = null
) {
    Card(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmailField(value = email, onValueChange = onEmailChange)

            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                isVisible = isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                onDone = onLoginClick
            )
            // Sera necesario un recuperar contraseñas o no? si no borra este text button noma


            // asumiria que si pero ni idea como implementarlo ahora

            // aya 👍

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(R.string.login_forgot_password))
            }

            LoginButton(onClick = onLoginClick)

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.login_email_label)) },
        leadingIcon = { Icon(imageVector = Icons.Rounded.Email, contentDescription = null) },
        singleLine = true,
        shape = FieldShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onDone: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.login_password_label)) },
        leadingIcon = { Icon(imageVector = Icons.Rounded.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                AnimatedContent(targetState = isVisible, label = "password_visibility_icon") { visible ->
                    Icon(
                        imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = stringResource(
                            if (visible) R.string.login_hide_password else R.string.login_show_password
                        )
                    )
                }
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        shape = FieldShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            }
        )
    )
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "login_button_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = FieldShape,
        interactionSource = interactionSource
    ) {
        Text(
            text = stringResource(R.string.login_button),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PanalSuiteTheme {
        LoginScreen(onLoginClick = {_ , _ -> })
    }
}
