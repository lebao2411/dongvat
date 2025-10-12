package com.example.endangeredanimals.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.endangeredanimals.R
import com.example.endangeredanimals.ui.AppGrayBlue
import com.example.endangeredanimals.ui.AppPrimaryColor
import com.example.endangeredanimals.ui.AppWarningColor

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Thông tin cá nhân",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                InfoTextField(
                    value = "Nguyen Van A",
                    label = "Tên người dùng",
                    icon = Icons.Default.Person
                )
                InfoTextField(
                    value = "nguyenvana@email.com",
                    label = "Email",
                    icon = Icons.Default.Email
                )
                InfoTextField(
                    value = "1,250 điểm",
                    label = "Điểm bảo vệ sinh cảnh",
                    iconPainter = painterResource(id = R.drawable.habitat)
                )
                InfoTextField(
                    value = "3,500 điểm",
                    label = "Điểm bảo tồn động vật",
                    iconPainter = painterResource(id = R.drawable.animal)
                )

                Button(
                    onClick = { /* TODO: Thêm logic sửa thông tin */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor)
                ) {
                    Text("Sửa thông tin")
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ActionButton(
                text = "Đổi mật khẩu",
                onClick = { /* TODO: Thêm logic đổi mật khẩu */ },
                modifier = Modifier.weight(1f),
                containerColor = AppGrayBlue,
                contentColor = Color.White
            )

            ActionButton(
                text = "Xóa tài khoản",
                onClick = { /* TODO: Thêm logic xóa tài khoản */ },
                modifier = Modifier.weight(1f),
                containerColor = AppWarningColor,
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun InfoTextField(value: String, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {  },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Composable
fun InfoTextField(value: String, label: String, iconPainter: Painter) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
