package edu.byu.cs.tweeter.client.presenter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.observer.AuthenticateTaskObserver;
import edu.byu.cs.tweeter.client.presenter.view.RegisterView;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class RegisterPresenter extends Presenter<RegisterView> {

    private UserService userService;

    public RegisterPresenter(RegisterView view) {
        this.view = view;
        this.userService = new UserService();
    }

    public void startRegisterTask(String firstName, String lastName, String alias, String password, Drawable imageDrawable) {
        try {
            validateRegistration(firstName, lastName, alias, password, imageDrawable);
            view.informRegisterReady();

            // Convert image to byte array.
            Bitmap image = ((BitmapDrawable) imageDrawable).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] imageBytes = bos.toByteArray();

            // Intentionally, Use the java Base64 encoder so it is compatible with M4.
            String imageBytesBase64 = Base64.getEncoder().encodeToString(imageBytes);

            userService.register(firstName, lastName,
                    alias, password, imageBytesBase64, new RegisterObserver());
        } catch (Exception e) {
            view.notifyException(e.getMessage());
        }
    }

    public void validateRegistration(String firstName, String lastName, String alias, String password, Drawable image) {
        if (firstName.length() == 0) {
            throw new IllegalArgumentException("First Name cannot be empty.");
        }
        if (lastName.length() == 0) {
            throw new IllegalArgumentException("Last Name cannot be empty.");
        }
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias cannot be empty.");
        }
        if (alias.charAt(0) != '@') {
            throw new IllegalArgumentException("Alias must begin with @.");
        }
        if (alias.length() < 2) {
            throw new IllegalArgumentException("Alias must contain 1 or more characters after the @.");
        }
        if (password.length() == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Profile image must be uploaded.");
        }
    }

    public class RegisterObserver implements AuthenticateTaskObserver {

        @Override
        public void handleSuccess(User registeredUser, AuthToken authToken) {
            Cache.getInstance().setCurrUser(registeredUser);
            Cache.getInstance().setCurrUserAuthToken(authToken);
            view.registerSuccess(registeredUser, Cache.getInstance().getCurrUser().getName());
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to register: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to register because of exception: " + ex.getMessage());
        }
    }
}
