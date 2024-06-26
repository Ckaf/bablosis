import {useRoutes} from "./routes";
import {BrowserRouter as Router} from "react-router-dom";
import {Toaster} from "react-hot-toast"
import {useAuthContext} from "./context/AuthContext";
import {useEffect} from "react";
import {Box, CssVarsProvider} from "@mui/joy";
import {Header} from "./components";

function App() {

	const {userData} = useAuthContext()

	const routes = useRoutes(userData)

	// useEffect(() => {
	// 	console.log('userData', userData)
	// }, [userData]);


	return (
		<Box sx={{display: "flex", flexDirection: 'column', width: '100%'}}>
			<Toaster
				position="bottom-right"
				reverseOrder={false}
			/>
			<Router>
				{userData && <Header/>}
				<CssVarsProvider>
					{routes}
				</CssVarsProvider>
			</Router>
		</Box>
	);
}

export default App;
