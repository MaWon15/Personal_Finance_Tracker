package com.example.personal_finance_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.repository.CategoryRepository
import com.example.personal_finance_tracker.data.repository.DeleteCategoryPolicy
import com.example.personal_finance_tracker.data.local.model.CategoryWithCount

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<CategoryWithCount> = emptyList(),
    val isMutating: Boolean = false,
    val errorMessage: String? = null
)

class CategoriesViewModel(
    private val categoryRepo: CategoryRepository,
    private val uid: String
) : ViewModel() {

    private val isMutating = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val categoriesFlow = categoryRepo.observeAllWithCounts(uid)

    val uiState: StateFlow<CategoriesUiState> =
        combine(categoriesFlow, isMutating, errorMessage) { cats, mutating, err ->
            CategoriesUiState(
                categories = cats,
                isMutating = mutating,
                errorMessage = err
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoriesUiState()
        )

    fun consumeError() {
        errorMessage.value = null
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            errorMessage.value = "Category name cannot be empty"
            return
        }
        viewModelScope.launch {
            isMutating.value = true
            try {
                categoryRepo.add(CategoryEntity(ownerUid = uid, name = trimmed))
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to add category"
            } finally {
                isMutating.value = false
            }
        }
    }

    fun updateCategory(categoryId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            errorMessage.value = "Category name cannot be empty"
            return
        }
        viewModelScope.launch {
            isMutating.value = true
            try {
                categoryRepo.update(
                    CategoryEntity(categoryId = categoryId, ownerUid = uid, name = trimmed)
                )
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to update category"
            } finally {
                isMutating.value = false
            }
        }
    }

    fun deleteCategoryWithPolicy(
        categoryId: Long,
        policy: DeleteCategoryPolicy,
        moveToCategoryId: Long? = null
    ) {
        viewModelScope.launch {
            isMutating.value = true
            try {
                categoryRepo.deleteCategoryWithPolicy(
                    uid = uid,
                    categoryId = categoryId,
                    policy = policy,
                    moveToCategoryId = moveToCategoryId
                )
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to delete category"
            } finally {
                isMutating.value = false
            }
        }
    }

    class Factory(
        private val categoryRepo: CategoryRepository,
        private val uid: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(categoryRepo, uid) as T
        }
    }
}
