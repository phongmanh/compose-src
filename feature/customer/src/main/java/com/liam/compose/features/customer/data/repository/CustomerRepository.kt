package com.liam.compose.features.customer.data.repository

import com.liam.compose.core.networking.model.AppResponse
import com.liam.compose.core.networking.remote.ErrorMapper
import com.liam.compose.core.networking.repository.BaseRepository
import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.data.model.SaveCustomerModel
import com.liam.compose.features.customer.data.model.SearchRequest
import com.liam.compose.features.customer.data.remote.CustomerService
import javax.inject.Inject

data class CustomerListResponse(
    val items: List<CustomerModel>,
    val totalCount: Int,
)

class CustomerRepository @Inject constructor(
    private val customerService: CustomerService,
    errorMapper: ErrorMapper,
) : BaseRepository(errorMapper) {

    suspend fun getCustomers(request: SearchRequest): Result<AppResponse<CustomerListResponse>> {
        return safeApiCall("getCustomers") {
            val response = customerService.searchMatrixForVisit(request)
            // Unwrap ResponseWrap into CustomerListResponse
            AppResponse(
                code = response.code,
                message = response.message,
                data = response.data?.let { wrap ->
                    CustomerListResponse(
                        items = wrap.data ?: emptyList(),
                        totalCount = wrap.recordsTotal ?: 0,
                    )
                },
                errors = response.errors
            )
        }
    }

    suspend fun saveCustomer(request: SaveCustomerModel): Result<AppResponse<Any>> {
        return safeApiCall("saveCustomer") { customerService.saveCustomer(request) }
    }

    suspend fun getCustomerById(customerId: Int): Result<AppResponse<CustomerModel>> {
        return safeApiCall("getCustomerById") {
            customerService.getCustomerById(mapOf("customerId" to customerId))
        }
    }

    suspend fun getSalesByRole(roleCode: String, userName: String): Result<AppResponse<List<OptionModel>>> {
        return safeApiCall("getSalesByRole") {
            customerService.getSalesByRole(mapOf("roleCode" to roleCode, "userName" to userName))
        }
    }

    suspend fun getRoutesByUser(userName: String): Result<AppResponse<List<OptionModel>>> {
        return safeApiCall("getRoutesByUser") {
            customerService.getRoutesByUser(mapOf("userName" to userName))
        }
    }

    suspend fun getProvinces(): Result<AppResponse<List<OptionModel>>> {
        return safeApiCall("getProvinces") {
            customerService.getProvinces(emptyMap())
        }
    }
}
