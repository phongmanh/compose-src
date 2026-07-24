package com.liam.compose.features.customer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveCustomerModel(
    val id: Int? = null,
    val code: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val customerTypeId: Int? = null,
    val provinceCode: String? = null,
    val note: String? = null,
    val taxCode: String? = null,
    val mainContactName: String? = null,
    val routeCode: String? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val updatedBy: String? = null,
    val updateDate: String? = null,
    val siSoHocSinh: Int? = null,
)
