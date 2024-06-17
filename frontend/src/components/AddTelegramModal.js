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

export const AddTelegramModal = ({isOpen, setIsOpen, refetch}) => {
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			tg: '',
		}
	})

	const onSubmit = async data => {
		request(`/add_tg`, 'POST', data).then(() => {
			toast.success('Телеграмм привязан')
			handleClose()
		}).catch(() => {})
	}

	const handleClose = () => {
		refetch()
		reset()
		setIsOpen(false)
	}

	return (
		<Modal open={isOpen} onClose={handleClose} variant="soft">
			<ModalDialog>
				<DialogTitle>Привязка телеграмма</DialogTitle>
				<Typography>Отсканируйте QR код и пройдите шаги в инструкции</Typography>
				<img src={qr} style={{width: '400px', height: '400px'}} alt='qr code gachi'/>
				<form
					onSubmit={handleSubmit(onSubmit)}
				>
					<Stack spacing={2}>
						<Controller
							name={"tg"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Введите код, который получился после прохождения инструкции </FormLabel>
									<Input autoFocus required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Stack direction="row" spacing={2}>
							<Button type="submit" variant="soft">Сохранить</Button>
							<Button color="danger" variant="soft" onClick={handleClose}>Отмена</Button>
						</Stack>
					</Stack>
				</form>
			</ModalDialog>
		</Modal>
	)
}
