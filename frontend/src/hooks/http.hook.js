import {useState, useCallback} from 'react'
import {BASE_URL} from "../config";
import {useAuthContext} from "../context/AuthContext";
import toast from "react-hot-toast";

export const useHttp = () => {
	const [loading, setLoading] = useState(false)
	const [error, setError] = useState(null)

	const {userData} = useAuthContext()

	const request = useCallback(async (url, method = 'POST', body = null, auth = true) => {
		setLoading(true)
		try {

			let options = {
				method: method,
				headers: {
					"Accept": "application/json",
					"Content-Type": "application/json",
				}
			}

			if (body) {
				options.body = JSON.stringify(body)
			}

			if (auth) {
				if (userData && userData.accessToken) {
					// options.headers.Authorization = `Bearer ${userData.accessToken}`
					const restBody = body ? body : {}
					options.body = JSON.stringify({
						accessToken: `${userData.accessToken}`,
						...restBody
					})
				} else throw new Error('Требуется авторизация')
			}

			const response = await fetch(`${BASE_URL}${url}`, options)
			let data = {}
			try {
				data = await response.json()
			} catch (e) {

			}

			if (!response.ok) {
				// toast.error(data.error)
				throw new Error(data.error || 'Что-то пошло не так во время выполнения запроса')
			}

			setLoading(false)

			return data
		} catch (e) {
			setLoading(false)
			setError(e.message)
			toast.error(e.message)
			throw e
		}
	}, [])

	const clearError = useCallback(() => setError(null), [])

	return { loading, request, error, clearError }
}
