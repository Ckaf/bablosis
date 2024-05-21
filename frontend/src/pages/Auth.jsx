import React from "react";
import BablosisLogo from "../components/BablosisLogo.jsx";
import AuthForm from "../components/AuthForm.jsx";

class Auth extends React.Component {
  render() {
    return (
      <div className="Auth">
        <BablosisLogo />
        <AuthForm />
      </div>
    );
  }
}

export default Auth;
