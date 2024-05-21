import React from "react";

class AuthForm extends React.Component {
  render() {
    return (
      <div className="AuthForm">
        <div className="FormRectangle">
          <h2>Login</h2>
          <form>
            <div className="user-box">
              <input type="text" />
              <label>e-mail</label>
            </div>
            <div className="user-box">
              <input type="password" />
              <label>Password</label>
            </div>
            <button className="button-18" role="button">
              Submit
            </button>
          </form>
        </div>
      </div>
    );
  }
}

export default AuthForm;
