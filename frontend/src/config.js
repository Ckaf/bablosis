export const BASE_URL = 'http://80.78.242.22:8080' // бэкенд (без / на конце!)
export const STORAGE_NAME = 'bablosisUserData' // название стора в localStorage (не трогать)
export const ROLES = ['ADMIN', 'USER', 'ISHTAR', 'COURIER', 'NONE'] // не трогать, что-то может сломаться
export const DELIVERY_STATUSES = { // Можно менять только значения, КЛЮЧИ НЕ ТРОГАТЬ
	'ASSEMBLY': 'Сборка',
	'DELIVERY': 'В пути',
	'DONE': 'Доставлен',
	'NONE': 'Ожидает курьера',
}
