package com.liam.compose.features.customer.data.remote

import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.data.model.ResponseWrap
import com.liam.compose.features.customer.data.model.SaveCustomerModel
import com.liam.compose.features.customer.data.model.SearchRequest
import com.liam.compose.core.networking.model.AppResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CustomerService {

    @POST("route/search-matrix-for-visit")
    @Headers("Content-Type: application/json")
    suspend fun searchMatrixForVisit(@Body request: SearchRequest): AppResponse<ResponseWrap<CustomerModel>>

    @POST("customer/save-customer")
    @Headers("Content-Type: application/json")
    suspend fun saveCustomer(@Body request: SaveCustomerModel): AppResponse<Any>

    @POST("customer/get-customer-by-id")
    @Headers("Content-Type: application/json")
    suspend fun getCustomerById(@Body request: Map<String, Int>): AppResponse<CustomerModel>

    @POST("route/get-list-sale-by-role")
    @Headers("Content-Type: application/json")
    suspend fun getSalesByRole(@Body request: Map<String, String>): AppResponse<List<OptionModel>>

    @POST("route/get-list-route-by-user")
    @Headers("Content-Type: application/json")
    suspend fun getRoutesByUser(@Body request: Map<String, String>): AppResponse<List<OptionModel>>

    @POST("common/get-list-province")
    @Headers("Content-Type: application/json")
    suspend fun getProvinces(@Body request: Map<String, String>): AppResponse<List<OptionModel>>
}
