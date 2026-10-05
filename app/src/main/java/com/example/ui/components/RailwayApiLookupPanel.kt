package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.RailwayApiEndpoint

@Composable
fun RailwayApiLookupPanel(
    response: String?,
    error: String?,
    isLoading: Boolean,
    onLookup: (String, RailwayApiEndpoint, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var serviceOrigin by rememberSaveable { mutableStateOf("") }
    var selectedEndpoint by rememberSaveable { mutableStateOf(RailwayApiEndpoint.TRAIN_SCHEDULE) }
    var requestJson by rememberSaveable { mutableStateOf("{}") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111B25)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E514C))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "INDIAN RAILWAY LOOKUP",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6EE7B7)
            )
            Text(
                text = "CRIS train and station services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                RailwayApiEndpoint.entries.forEach { endpoint ->
                    val selected = endpoint == selectedEndpoint
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Color(0xFF6EE7B7) else Color(0xFF1B2933),
                        modifier = Modifier.clickable { selectedEndpoint = endpoint }
                    ) {
                        Text(
                            text = endpoint.title,
                            color = if (selected) Color(0xFF10211C) else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Text(
                text = "POST /CRISApi/ws1/nget/${selectedEndpoint.path}",
                fontSize = 11.sp,
                color = Color(0xFF9BB6B1)
            )

            OutlinedTextField(
                value = serviceOrigin,
                onValueChange = { serviceOrigin = it },
                label = { Text("Railway API service origin") },
                placeholder = { Text("https://your-api-host") },
                singleLine = true,
                colors = fieldColors(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = requestJson,
                onValueChange = { requestJson = it },
                label = { Text("POST body (JSON)") },
                supportingText = { Text("Enter the fields required by your CRIS API provider.") },
                minLines = 3,
                maxLines = 6,
                colors = fieldColors(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onLookup(serviceOrigin, selectedEndpoint, requestJson) },
                enabled = !isLoading && serviceOrigin.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6EE7B7)),
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        color = Color(0xFF10211C),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                } else {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF10211C)
                    )
                    Spacer(Modifier.padding(horizontal = 3.dp))
                }
                Text(
                    text = if (isLoading) "Requesting" else "Send railway request",
                    color = Color(0xFF10211C),
                    fontWeight = FontWeight.Bold
                )
            }

            error?.let {
                Text(text = it, color = Color(0xFFFCA5A5), fontSize = 12.sp)
            }

            response?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .background(Color(0xFF09120F), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFD1FAE5),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF6EE7B7),
    unfocusedBorderColor = Color(0xFF3B4D55),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF6EE7B7),
    unfocusedLabelColor = Color(0xFF9BB6B1),
    focusedContainerColor = Color(0xFF0B141B),
    unfocusedContainerColor = Color(0xFF0B141B),
    cursorColor = Color(0xFF6EE7B7)
)