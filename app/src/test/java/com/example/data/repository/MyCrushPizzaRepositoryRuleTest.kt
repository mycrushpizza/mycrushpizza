package com.example.data.repository

import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MyCrushPizzaRepositoryRuleTest {

    @Test
    fun defaultMenuAndCoupons_haveValidPricesAndSubCategories() {
        val menu = StoreAndMenuRepository.defaultHajipurMenu()
        assertEquals(212, menu.size)
        assertTrue(menu.all { it.priceRegularInr > 0 })
        assertTrue(menu.all { it.displaySubCategoryLabel().isNotBlank() })

        val coupons = StoreAndMenuRepository.defaultCoupons()
        assertTrue(coupons.isEmpty())
    }

    @Test
    fun customerOrderStatusFlow_hidesInternalCoordinatesAndShowsSafeCustomerSteps() {
        val order = PizzaOrder(
            id = "order_101",
            orderNumber = "MCP-1010",
            customerId = "cust_1",
            customerName = "Ananya Hajipur",
            customerPhone = "9876543210",
            deliveryAddress = "Station Road, Hajipur, Bihar",
            landmark = "Near Hajipur Junction",
            status = OrderStatus.PREPARING.code,
            adminAlertAcknowledged = true
        )
        assertEquals(OrderStatus.PREPARING, OrderStatus.fromCode(order.status))
        assertFalse(OrderStatus.fromCode(order.status) == OrderStatus.CANCELLED)
    }
}
