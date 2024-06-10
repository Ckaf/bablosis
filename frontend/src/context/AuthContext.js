import {createContext, useCallback, useContext, useEffect, useState} from "react";
// import {getProfile, signOut} from "../api/api";
import toast from "react-hot-toast";
import {clearLocalStorage, getUserDataFromLocalStorage, setUserDataInLocalStorage} from "../utils/localStorage";

const Auth = createContext({})

const AuthContext = ({children}) => {
	const [userData, setUserData] = useState(null)
	const [shouldUpdate, setShouldUpdate] = useState(false)

	// const getToken = useCallback(
	// 	() => {
	// 		const token = localStorage.getItem('token')
	// 		if (token) {
	// 			setAccessToken(token)
	// 		} else setAccessToken(null)
	// 	},
	// 	[setAccessToken],
	// )


	const getUserData = useCallback(
		() => {
			const data = getUserDataFromLocalStorage()
			login(data)
		},
		[]
	)

	const login = useCallback(
		 (data) => {
			 // console.log('login', data)
			 if (data && data.accessToken) {
				 setUserData(data)
			 }
			 setUserDataInLocalStorage(data)
		},
		[]
	)

	const logout = useCallback(() => {
		setUserData(null)
		clearLocalStorage()
		// toast.success('Выход из аккаунта')
	}, [])

	const update = useCallback(
		() => {
			setShouldUpdate(prevState => !prevState)
		},
		[]
	)

	// useEffect(() => {
	// 	getToken()
	// }, [shouldUpdate])

	useEffect(() => {
		getUserData()
	}, [shouldUpdate])

	return (
		<Auth.Provider value={{
			userData,
			setUserData,
			update,
			login,
			logout
		}}>
			{children}
		</Auth.Provider>
	)
}

const useAuthContext = () => useContext(Auth)

export { AuthContext, useAuthContext }
