import React from 'react';
import {BrowserRouter, Routes, Route} from 'react-router-dom'
import Registration from './pages/Registration.jsx';
import Auth from './pages/Auth.jsx';


class App extends React.Component{
  render(){
 return (
   <div className="App">
  
   <BrowserRouter> 
    <Routes>
      <Route path="/registration" element={<Registration/>}/>
      <Route path="/" element={<Auth/>}/>
    </Routes>
   </BrowserRouter>

   </div>
  );
  }
}

export default App;
