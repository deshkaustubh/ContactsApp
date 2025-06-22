package com.example.contactsapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            context = applicationContext,
            klass = ContactDatabase::class.java,
            name = "contact_database",
        ).build()

        val repository = ContactRepository(database.contactDao())

        val viewmodel: ContactViewModel by viewModels { ContactViewModelFactory(repository) }

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "contactList") {
                composable("contactList") {
                    ContactListScreen(viewModel = viewmodel, navController = navController)
                }
                composable("addContact") {
                    AddContactScreen(viewModel = viewmodel, navController = navController)
                }
                composable("contactDetail/{contactId}") { backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
                    contactId?.let { id ->
                        val contact by viewmodel.allContacts.observeAsState(initial = emptyList())
                        val selectedContact = contact.find { it.id == id }
                        selectedContact?.let {
                            ContactDetailScreen(
                                contact = it,
                                viewModel = viewmodel,
                                navController = navController
                            )
                        } ?: run {
                            Toast.makeText(
                                this@MainActivity,
                                "Contact not found",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("contactList")
                        }
                    }
                }
                composable("editContact/{contactId}") { backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
                    contactId?.let { id ->
                        val contact by viewmodel.allContacts.observeAsState(initial = emptyList())
                        val selectedContact = contact.find { it.id == id }
                        selectedContact?.let {
                            EditContactScreen(contact = it, viewModel = viewmodel, navController = navController)
                        } ?: run {
                            Toast.makeText(
                                this@MainActivity,
                                "Contact not found",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("contactList")
                        }
                    }
                }
            }
        }
    }
}

// Addition Screen for adding a new contact
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var name by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }
    var phoneNumber by remember {
        mutableStateOf("")
    }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("ADD CONTACT", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()) {
                        val imagePath = imageUri?.toString() ?: ""
                        viewModel.addContact(imagePath, phoneNumber, email, name)
                        navController.popBackStack()
                        Toast.makeText(context, "Contact added!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Save Contact",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri), contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape), contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "add contact",
                    modifier = Modifier.size(128.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Choose Image", color = Color.White)
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    // Allow saving even if no image is selected
                    val imagePath = imageUri?.toString() ?: ""
                    viewModel.addContact(imagePath, phoneNumber, email, name)
                    navController.navigate(route = "contactList") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Save Contact", color = Color.White)
            }
        }

    }
}


fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    val file = File(context.filesDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ContactItem Composable to display each contact in a list
@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (contact.image.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(contact.image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "contact image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = contact.name)
        }
    }
}

// ContactListScreen Composable to display the list of contacts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current.applicationContext
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("Contacts", fontSize = 18.sp, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Contacts", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Contact",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { navController.navigate("addContact") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Contact",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val contacts by viewModel.allContacts.observeAsState(initial = emptyList())
            if (contacts.isEmpty()) {
                Text(text = "No Contacts Available", modifier = Modifier.padding(16.dp))
            } else {
                contacts.forEach { contact ->
                    ContactItem(contact = contact) {
                        navController.navigate("contactDetail/${contact.id}")
                    }
                }
            }
        }
    }
}

// ContactDetailScreen Composable to display the details of a selected contact
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: Contact,
    viewModel: ContactViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = contact.name, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("contactList") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            IconButton(
                onClick = {
                    navController.navigate("editContact/${contact.id}")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Contact",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contact image with better error handling
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (contact.image.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = contact.image,
                            onError = {
                                // Handle image loading error silently
                            }
                        ),
                        contentDescription = "Contact image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Contact image",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Name: ${contact.name}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Phone: ${contact.phoneNumber}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Email: ${contact.email}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete button
            Button(
                onClick = {
                    viewModel.deleteContact(contact)
                    navController.navigate("contactList") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Delete Contact", color = Color.White)
            }
        }
    }
}


// EditContactScreen Composable to edit an existing contact
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(contact: Contact, viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current
    var imageUri by remember {
        mutableStateOf<Uri?>(
            if (contact.image.isNotEmpty()) {
                Uri.fromFile(File(contact.image))
            } else {
                null
            }
        )
    }
    var name by remember {
        mutableStateOf(contact.name)
    }
    var email by remember {
        mutableStateOf(contact.email)
    }
    var phoneNumber by remember {
        mutableStateOf(contact.phoneNumber)
    }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { newUri ->
                val internalPath = copyUriToInternalStorage(context, newUri, "${contact.name}.jpg")
                if (internalPath != null) {
                    imageUri = Uri.fromFile(File(internalPath))
                } else {
                    Toast.makeText(context, "Failed to update image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("EDIT CONTACT", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Edit Contact", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "edit contact"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri), contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape), contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "contact image",
                    modifier = Modifier.size(128.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Change Image", color = Color.White)
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    val finalImagePath = imageUri?.let { uri ->
                        if (uri.scheme == "content") {
                            copyUriToInternalStorage(context, uri, "$name.jpg")
                        } else {
                            contact.image // Keep original path if image not changed
                        }
                    } ?: contact.image // Keep original image path if no new image selected

                    if (name.isNotEmpty()) {
                        viewModel.updateContact(
                            Contact(
                                id = contact.id,
                                image = finalImagePath,
                                phoneNumber = phoneNumber,
                                email = email,
                                name = name
                            )
                        )
                        navController.navigate(route = "contactList") {
                            popUpTo(0)
                        }
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Update Contact", color = Color.White)
            }
        }
    }
}
