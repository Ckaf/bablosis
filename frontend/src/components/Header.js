import * as React from 'react';
import Box from '@mui/joy/Box';
import Typography from '@mui/joy/Typography';
import {useAuthContext} from "../context/AuthContext";
import {Button, Link} from "@mui/joy";

export const Header = () => {
	const {userData, logout} = useAuthContext()

	const logoutHandler = () => {
		logout()
	}

	return (
		<Box
			sx={{
				display: 'flex',
				flexGrow: 1,
				justifyContent: 'space-between',
				alignItems: 'center',
				backgroundColor: '#E3F2FD',
				height: '80px',
				padding: '0 25px'
			}}
		>
			<Link href={'/'} underline="none" level="h1" color="primary" variant="plain">BABLOSIS</Link>
			<Typography level="h4" color="primary" variant="soft">{userData.user.name}</Typography>
			<Button  color="primary" variant="soft" onClick={logoutHandler}>Выход</Button>
		</Box>
	);
}
