package com.uniflow.app.ui

import com.uniflow.app.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var userPreferences: UserPreferences = mock(UserPreferences::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = mock(UserPreferences::class.java)
        `when`(userPreferences.userName).thenReturn(flowOf(null))
        `when`(userPreferences.userDepartment).thenReturn(flowOf(null))
        `when`(userPreferences.userPosition).thenReturn(flowOf(null))
        `when`(userPreferences.userRole).thenReturn(flowOf("Lecturer"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `MainViewModel initializes correctly`() {
        val viewModel = MainViewModel(userPreferences)
        assert(viewModel.userName.value == null)
    }
}
