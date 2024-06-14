import {STORAGE_NAME} from "../config";

export const setUserDataInLocalStorage = (data) => {
	localStorage.setItem(STORAGE_NAME, JSON.stringify(data))
}

export const getUserDataFromLocalStorage = () => {
	const data = JSON.parse(localStorage.getItem(STORAGE_NAME) || null)
	// console.log(data)
	return data
}

export const clearLocalStorage = () => {
	localStorage.removeItem(STORAGE_NAME)
}
