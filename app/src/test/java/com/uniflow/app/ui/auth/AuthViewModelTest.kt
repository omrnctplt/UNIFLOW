package com.uniflow.app.ui.auth

import com.uniflow.app.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var authRepository: AuthRepository = mock(AuthRepository::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock(AuthRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with blank username returns error`() = runTest {
        val viewModel = AuthViewModel(authRepository)
        viewModel.login("", "password123")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assert(state is AuthState.Error)
        assertEquals("Lütfen tüm alanları doldurun.", (state as AuthState.Error).message)
    }

    @Test
    fun `login with blank password returns error`() = runTest {
        val viewModel = AuthViewModel(authRepository)
        viewModel.login("user", "")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assert(state is AuthState.Error)
        assertEquals("Lütfen tüm alanları doldurun.", (state as AuthState.Error).message)
    }
}
