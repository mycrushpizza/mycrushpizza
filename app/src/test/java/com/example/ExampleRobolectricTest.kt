package com.example

import com.example.data.repository.StoreAndMenuRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `read store config defaults`() {
        val config = StoreAndMenuRepository.defaultStoreConfig()
        assertEquals(299, config.freeDeliveryMinOrderInr)
    }
}
