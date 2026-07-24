package com.liam.compose.features.customer.ui.state

import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.data.model.SaveCustomerModel

sealed interface CustomerFormUiState {
    data object Idle : CustomerFormUiState

    data class Loading(val draft: SaveCustomerModel) : CustomerFormUiState

    data class Initialized(
        val customer: CustomerModel?,
        val salesOptions: List<OptionModel>,
        val routeOptions: List<OptionModel>,
        val provinceOptions: List<OptionModel>,
    ) : CustomerFormUiState

    data object Saving : CustomerFormUiState

    data class Success(val customerId: Int) : CustomerFormUiState

    data class Error(val message: String) : CustomerFormUiState
}
