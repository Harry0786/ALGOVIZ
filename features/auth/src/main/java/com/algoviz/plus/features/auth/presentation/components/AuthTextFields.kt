package com.algoviz.plus.features.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Email",
    placeholder: String = "name@company.com",
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        label = label?.takeIf { it.isNotBlank() }?.let { { Text(it) } },
        placeholder = { Text(text = placeholder, fontSize = 15.sp, color = Color.Black) },
        textStyle = TextStyle(fontSize = 15.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(32.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111111),
            unfocusedTextColor = Color(0xFF111111),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color(0xFFE8E8EA),
            unfocusedContainerColor = Color(0xFFE8E8EA),
            focusedLabelColor = Color(0xFF111111),
            unfocusedLabelColor = Color(0xFF6B7280),
            cursorColor = Color(0xFF111111),
            focusedPlaceholderColor = Color.Black,
            unfocusedPlaceholderColor = Color.Black
        )
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Password",
    placeholder: String = "••••••••",
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        label = label?.takeIf { it.isNotBlank() }?.let { { Text(it) } },
        placeholder = { Text(text = placeholder, fontSize = 15.sp, color = Color.Black) },
        textStyle = TextStyle(
            fontSize = 15.sp,
            letterSpacing = if (passwordVisible) 0.sp else 2.sp
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(32.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111111),
            unfocusedTextColor = Color(0xFF111111),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color(0xFFE8E8EA),
            unfocusedContainerColor = Color(0xFFE8E8EA),
            focusedLabelColor = Color(0xFF111111),
            unfocusedLabelColor = Color(0xFF6B7280),
            cursorColor = Color(0xFF111111),
            focusedPlaceholderColor = Color.Black,
            unfocusedPlaceholderColor = Color.Black
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = Color.Black
                )
            }
        }
    )
}
