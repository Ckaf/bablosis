import {
	Button,
	DialogTitle,
	FormControl,
	FormLabel, Input,
	Modal,
	ModalDialog,
	Option,
	Select,
	Stack,
	Typography
} from "@mui/joy";
import {Controller, useForm} from "react-hook-form";
import {ROLES} from "../config";
import * as React from "react";
import {useHttp} from "../hooks/http.hook";
import {useAuthContext} from "../context/AuthContext";
import toast from "react-hot-toast";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";

export const AddOrderModal = ({isOpen, setIsOpen}) => {
	const [balance, setBalance] = useState(0);
	const {request} = useHttp()
	const {userData} = useAuthContext()
	const navigate = useNavigate()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			bablos: '',
			address: '',
		}
	})

	const onSubmit = async data => {
		request(`/create_order`, 'POST', data).then(() => {
			toast.success('Заказ создан')
			handleClose()
			navigate('/my-orders')
		}).catch(() => {})
	}

	const getBalance = () => {
		request(`/get_balance`, 'POST').then((data) => {
			setBalance(data.balance)
		}).catch(() => {})
	}

	const handleClose = () => {
		reset()
		setIsOpen(false)
	}

	useEffect(() => {
		getBalance()
	}, [isOpen]);


	return (
		<Modal open={isOpen} onClose={handleClose} variant="soft">
			<ModalDialog>
				<DialogTitle>Заказать доставку баблоса</DialogTitle>
				<Typography>баланс: {balance}</Typography>
				<form
					onSubmit={handleSubmit(onSubmit)}
				>
					<Stack spacing={2}>
						<Controller
							name={"address"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Адрес доставки</FormLabel>
									<Input autoFocus required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Controller
							name={"bablos"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Количество баблосов</FormLabel>
									<Input autoFocus required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Stack direction="row" spacing={2}>
							<Button type="submit" variant="soft">Заказать</Button>
							<Button color="danger" variant="soft" onClick={handleClose}>Отмена</Button>
						</Stack>
					</Stack>
				</form>
			</ModalDialog>
		</Modal>
	)
}
