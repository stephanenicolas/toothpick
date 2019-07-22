package toothpick.smoothie.viewmodel

import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel()

@javax.inject.Scope
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ViewModelScope