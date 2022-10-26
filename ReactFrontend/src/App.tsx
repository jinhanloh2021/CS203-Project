import { Routes, Route, useLocation } from "react-router-dom";
import Home from "pages/Home/Home";
import NotFound from "pages/404";
import Login from "pages/Login/Login";
import SignUp from "pages/SignUp/SignUp";
import Garden from "pages/Garden/Garden";
import Landing from "pages/Landing/Landing";


function App() {
  const location = useLocation();
  // use the location to determine if the user is logged in
  return (
    <Routes location={location}>
      {/* Accessible to all */}
      <Route path="/" element={<Landing />} />
      <Route path="/Landing" element={<Landing />} />
      <Route path="/home" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/sign-up" element={<SignUp />} />
      <Route path="/garden" element={<Garden />} />
      {/* Accessible to NOT logged in users */}

      <Route path="*" element={<Login />} />
    </Routes>
  );
}

export default App;
