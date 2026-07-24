plugins {
    // Android baseline + Compose/serialization come from the convention plugins.
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
}

android {
    namespace = "com.liam.compose.core.components"
}

dependencies {
    // Exposed as `api` so consumers can use Material3 types transitively.
    api(libs.androidx.compose.material3)
    // `api` so a feature's ViewModel/screen gets Paging 3 (Pager/PagingData/LazyPagingItems)
    // transitively — PagedList takes LazyPagingItems in its signature, and pagerOf returns a Pager.
    api(libs.androidx.paging.compose)
}
