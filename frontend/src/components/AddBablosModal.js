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
import qr from '../assets/QR.png'

export const AddBablosModal = ({isOpen, setIsOpen}) => {
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			bablos: '',
		}
	})

	const onSubmit = async data => {
		request(`/set_bablos`, 'POST', data).then(() => {
			toast.success('Баблос поставлен')
			handleClose()
		}).catch(() => {})
	}

	const handleClose = () => {
		reset()
		setIsOpen(false)
	}

	return (
		<Modal open={isOpen} onClose={handleClose} variant="soft">
			<ModalDialog>
				<DialogTitle>Поставка баблоса</DialogTitle>
				<form
					onSubmit={handleSubmit(onSubmit)}
				>
					<Stack spacing={2}>
						<Controller
							name={"bablos"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Количество</FormLabel>
									<Input autoFocus required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Stack direction="row" spacing={2}>
							<Button type="submit" variant="soft">Ввести поставку</Button>
							<Button color="danger" variant="soft" onClick={handleClose}>Отмена</Button>
						</Stack>
					</Stack>
				</form>
			</ModalDialog>
		</Modal>
	)
}
