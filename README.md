# ContactsApp 📱

ContactsApp is a simple Android application built with Kotlin and Jetpack Compose that allows users to manage their personal contacts. The app uses Room for local database storage and supports adding, editing, viewing, and deleting contacts. Each contact can have a name, phone number, email, and an optional profile image.

## Features ✨
- ➕ Add new contacts with name, phone number, email, and image
- ✏️ Edit existing contacts
- 🗑️ Delete contacts
- 📋 View a list of all contacts
- 🔍 View detailed information for each contact
- 💾 Persist data locally using Room database
- 🎨 Modern UI with Jetpack Compose

## Tech Stack 🛠️
- Kotlin
- Jetpack Compose
- Room Database
- MVVM Architecture

## Getting Started 🚀
1. Clone the repository or download the source code.
2. Open the project in Android Studio.
3. Build and run the app on an emulator or Android device.

## Folder Structure 🗂️
- `app/src/main/java/com/example/contactsapp/` - Main source code (models, viewmodels, repository, UI)
- `app/src/main/res/` - Resources (layouts, drawables, etc.)
- `app/src/main/AndroidManifest.xml` - App manifest

## Implementation Guide 📝

Follow these steps to build a simple Contacts App using Kotlin, Jetpack Compose, and Room. Each step includes code examples from this project for clarity.

### 1. Create the Data Class
Define a data class for your contact entity. Annotate it with `@Entity` and add a primary key.

```kotlin
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val image: String,
    val phoneNumber: String,
    val email: String,
    val name: String
)
```

### 2. Add Dependencies 📦
Add the required dependencies for Room, Lifecycle, Navigation, and Compose in your `build.gradle.kts`:

```kotlin
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
implementation("androidx.navigation:navigation-compose:2.7.7")
implementation("io.coil-kt:coil-compose:2.1.0")
ksp("androidx.room:room-compiler:2.6.1")
```

### 3. Create the DAO (Data Access Object) 🗃️
Define an interface for database operations:

```kotlin
@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)
    @Update
    suspend fun update(contact: Contact)
    @Delete
    suspend fun delete(contact: Contact)
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<Contact>>
}
```

### 4. Create the Database 🏦
Create an abstract class extending `RoomDatabase`:

```kotlin
@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
```

### 5. Create the Repository 📚
The repository abstracts access to multiple data sources:

```kotlin
class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    suspend fun insert(contact: Contact) = contactDao.insert(contact)
    suspend fun update(contact: Contact) = contactDao.update(contact)
    suspend fun delete(contact: Contact) = contactDao.delete(contact)
}
```

### 6. Create the ViewModel 🧠
The ViewModel provides data to the UI and handles logic:

```kotlin
class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    val allContacts: LiveData<List<Contact>> = repository.allContacts.asLiveData()
    fun addContact(image: String, phoneNumber: String, email: String, name: String) {
        viewModelScope.launch {
            val contact = Contact(0, image, phoneNumber, email, name)
            repository.insert(contact)
        }
    }
    fun deleteContact(contact: Contact) {
        viewModelScope.launch { repository.delete(contact) }
    }
    fun updateContact(contact: Contact) {
        viewModelScope.launch { repository.update(contact) }
    }
}
```

### 7. Set Up the MainActivity 🏁
Initialize the database, repository, and ViewModel. Set up navigation and screens:

```kotlin
val database = Room.databaseBuilder(
    context = applicationContext,
    klass = ContactDatabase::class.java,
    name = "contact_database",
).build()
val repository = ContactRepository(database.contactDao())
val viewmodel: ContactViewModel by viewModels { ContactViewModelFactory(repository) }
```

### 8. Build the UI with Jetpack Compose 🎨
Create composable functions for listing, adding, editing, and viewing contacts. Example for adding a contact:

```kotlin
@Composable
fun AddContactScreen(viewModel: ContactViewModel, navController: NavController) {
    // ...UI code for input fields and save button...
    Button(onClick = {
        viewModel.addContact(imagePath, phoneNumber, email, name)
        navController.popBackStack()
    }) {
        Text(text = "Save Contact")
    }
}
```

### 9. Run the App ▶️
Build and run the app on an emulator or device. You can now add, edit, view, and delete contacts.

## License 📄
This project is for educational purposes.
