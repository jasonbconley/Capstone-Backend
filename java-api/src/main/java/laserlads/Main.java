package laserlads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@RestController
class Count {

    @RequestMapping("/")
    String index() {
        return "This is the start of the index";
    }

    @RequestMapping("/count/{lotId}")
    int getCount(HttpServletResponse response,
              @PathVariable String lotId) {
        response.addHeader("Access-Control-Allow-Origin","*");
        int spacesAvailable = 0;
        try {
            spacesAvailable = CountData.fetchCount(lotId);
        }
        catch (SQLException e ) {
            e.printStackTrace();
        }

        if (spacesAvailable == -1) {
            response.setStatus(500);
        }
        return spacesAvailable;
    }

    @RequestMapping(value = "/lot/{lotId}/taken/{taken}/total/{total}", method = RequestMethod.POST)
    String updateData(HttpServletResponse response, HttpServletRequest request,
                      @PathVariable String lotId,
                      @PathVariable int taken,
                      @PathVariable int total) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            boolean token = (boolean) request.getSession().getAttribute("admin");
            if (token) {
                response.addHeader("Access-Control-Allow-Origin", "*");
                try {
                    if (total == 0) {
                        return CountData.updateTaken(lotId, taken);
                    } else {
                        return CountData.updateLot(lotId, taken, total);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Update failed";
                }
            } else {
                return "Unauthorized Action";
            }
        }
        else {
            return "Not logged in";
        }
    }

    @RequestMapping(value = "/newlot/{lotId}/taken/{taken}/total/{total}", method = RequestMethod.POST)
    String newLot(HttpServletResponse response, HttpServletRequest request,
                      @PathVariable String lotId,
                      @PathVariable int taken,
                      @PathVariable int total) {
        response.addHeader("Access-Control-Allow-Origin","*");

        HttpSession session = request.getSession(false);
        if (session != null) {
            boolean token = (boolean) request.getSession().getAttribute("admin");
            if (token) {
                try {
                    return CountData.addNewLot(lotId, taken, total);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Update failed";
                }
            }
            else {
                return "Unauthorized action";
            }
        } else {
            return "Not logged in";
        }
    }

    @RequestMapping(value = "/droplot/{lotId}", method = RequestMethod.POST)
    String deleteLot(HttpServletResponse response, HttpServletRequest request,
                     @PathVariable String lotId) {
        response.addHeader("Access-Control-Allow-Origin","*");

        HttpSession session = request.getSession(false);
        if (session != null) {
            boolean token = (boolean) request.getSession().getAttribute("admin");
            if (token) {
                try {
                    return CountData.dropLot(lotId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Update failed";
                }
            } else {
                return "Unauthorized action";
            }
        }
        else {
            return "Not logged in";
        }
    }

    @RequestMapping(value = "/register/email/{email}/pass/{pswd}", method = RequestMethod.POST)
    String createNewUser(HttpServletResponse response,
                     @PathVariable String email,
                     @PathVariable String pswd) {
        response.addHeader("Access-Control-Allow-Origin","*");

        try {
            if (CountData.checkForUser(email)) {
                return "User already in System";
            } else {
                if (CountData.addNewUser(email, Laser_Bcrypt.get_bcrypt_password(pswd))) {
                    return "User successfully registered";
                }
                else {
                    return "Failed to add new user";
                }
            }
        }
        catch (SQLException e) {
              e.printStackTrace();
              return "Failed to add new user: SQL Exception";
        }

    }

    @RequestMapping(value = "/login/email/{email}/pass/{pswd}", method = RequestMethod.POST)
    Hashtable<String, Boolean> loginUser(HttpServletResponse response, HttpServletRequest request,
                  @PathVariable String email,
                  @PathVariable String pswd) {
        response.addHeader("Access-Control-Allow-Origin","*");

        try {
            if (CountData.checkForUser(email)) {
                if (CountData.passwordMatch(email, pswd)) {

                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        session = request.getSession();
                        session.setMaxInactiveInterval(600);
                    }

                    if (CountData.isAdmin(email)) {
                        session.setAttribute("admin", true);
                    } else {
                        session.setAttribute("admin", false);
                    }

                    return loginVals.get(0);
                }
                else {
                    return loginVals.get(1);
                }
            } else {
                return loginVals.get(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return loginVals.get(1);
        }

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    boolean loggedIn(HttpServletResponse response, HttpServletRequest request) {
        response.addHeader("Access-Control-Allow-Origin","*");

        HttpSession session = request.getSession(false);
        if (session != null) {
            return true;
        } else {
            return false;
        }
    }

    static List<Hashtable<String, Boolean>> loginVals() {
        Hashtable<String, Boolean> success = new Hashtable<>();
        success.put("Authenticated", true);

        Hashtable<String, Boolean> failure = new Hashtable<>();
        failure.put("Authenticated", false);

        List<Hashtable<String, Boolean>> values = new ArrayList<Hashtable<String, Boolean>>();
        values.add(0,success);
        values.add(1,failure);
        List<Hashtable<String, Boolean>> logins = values.stream().toList();
        return logins;
    }

    static List<Hashtable<String, Boolean>> loginVals = loginVals();

}

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
