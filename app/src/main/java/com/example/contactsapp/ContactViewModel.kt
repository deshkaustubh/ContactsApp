package com.example.contactsapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    val allContacts: LiveData<List<Contact>> = repository.allContacts.asLiveData()

    fun addContact(image: String, phoneNumber: String, email: String, name: String) {
        viewModelScope.launch {
            val contact = Contact(
                id = 0,
                image = image,
                phoneNumber = phoneNumber,
                email = email,
                name = name
            )
            repository.insert(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.delete(contact)
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.update(contact)
        }
    }

}


class ContactViewModelFactory(private val repository: ContactRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown View Model class")
        }
    }
}