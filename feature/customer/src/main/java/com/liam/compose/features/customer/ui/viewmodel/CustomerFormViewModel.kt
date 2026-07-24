package com.liam.compose.features.customer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.data.model.SaveCustomerModel
import com.liam.compose.features.customer.data.repository.CustomerRepository
import com.liam.compose.features.customer.ui.state.CustomerFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val userPref: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerFormUiState>(CustomerFormUiState.Idle)
    val uiState: StateFlow<CustomerFormUiState> = _uiState.asStateFlow()

    // customerId comes from nav key (0 = create, > 0 = edit)
    private val customerId: Int = savedStateHandle.get<Int>("customerId") ?: 0

    init {
        viewModelScope.launch {
            initialize()
        }
    }

    suspend fun initialize() {
        val draft = getSavedDraft()
        _uiState.value = CustomerFormUiState.Loading(draft)

        try {
            // Load filter options in parallel
            val userName = userPref.userModel.first().userName ?: ""
            val userRole = userPref.userModel.first().role ?: ""

            val salesResult = repository.getSalesByRole(userRole, userName)
            val routeResult = repository.getRoutesByUser(userName)
            val provinceResult = repository.getProvinces()

            val salesOptions = salesResult.getOrNull()?.data ?: emptyList()
            val routeOptions = routeResult.getOrNull()?.data ?: emptyList()
            val provinceOptions = provinceResult.getOrNull()?.data ?: emptyList()

            var customer: CustomerModel? = null

            // If editing, fetch the customer data
            if (customerId > 0) {
                val customerResult = repository.getCustomerById(customerId)
                customer = customerResult.getOrNull()?.data
            }

            _uiState.value = CustomerFormUiState.Initialized(
                customer = customer,
                salesOptions = salesOptions,
                routeOptions = routeOptions,
                provinceOptions = provinceOptions,
            )
        } catch (e: Exception) {
            _uiState.value = CustomerFormUiState.Error(e.message ?: "Failed to initialize form")
        }
    }

    suspend fun saveCustomer(model: SaveCustomerModel) {
        // Validate required fields
        val validationError = validateCustomer(model)
        if (validationError != null) {
            _uiState.value = CustomerFormUiState.Error(validationError)
            return
        }

        _uiState.value = CustomerFormUiState.Saving

        val result = repository.saveCustomer(model)

        result.onSuccess { response ->
            // Extract customer ID from the response (success indicator)
            _uiState.value = CustomerFormUiState.Success(model.id ?: 0)
            clearSavedDraft()
        }

        result.onFailure { error ->
            _uiState.value = CustomerFormUiState.Error(error.message ?: "Failed to save customer")
        }
    }

    fun updateDraft(model: SaveCustomerModel) {
        saveDraft(model)
    }

    private fun validateCustomer(model: SaveCustomerModel): String? {
        if (model.name.isNullOrBlank()) {
            return "Customer name is required"
        }
        if (model.phone.isNullOrBlank()) {
            return "Phone number is required"
        }
        if (model.address.isNullOrBlank()) {
            return "Address is required"
        }
        return null
    }

    fun getSavedDraft(): SaveCustomerModel {
        val draftJson = savedStateHandle.get<String>("draft")
        return if (draftJson != null) {
            try {
                Gson().fromJson(draftJson, SaveCustomerModel::class.java)
            } catch (e: Exception) {
                SaveCustomerModel()
            }
        } else {
            SaveCustomerModel()
        }
    }

    private fun saveDraft(model: SaveCustomerModel) {
        val draftJson = Gson().toJson(model)
        savedStateHandle["draft"] = draftJson
    }

    private fun clearSavedDraft() {
        val emptyJson = Gson().toJson(SaveCustomerModel())
        savedStateHandle["draft"] = emptyJson
    }
}
