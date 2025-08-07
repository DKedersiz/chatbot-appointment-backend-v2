package com.dogukan.appointment.dto;

import com.dogukan.appointment.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DtoRegisterRequest {

    @NotBlank(message = "TC Kimlik numarası boş olamaz.")
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Geçerli bir TC Kimlik numarası giriniz.")
    private String tckNo;

    @NotBlank(message = "E-posta boş olamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotBlank(message = "Şifre boş olamaz.")
    @Size(min = 8, message = "Şifre en az 8 karakter içermelidir.")
    private String password;

    @NotBlank(message = "Ad kısmı boş olamaz.")
    @Size(min = 3, max = 15, message = "Adınız 3 ile 15 karakter arası olmalıdır.")
    private String firstName;

    @NotBlank(message = "Soyad kısmı boş olamaz.")
    @Size(min = 2, max = 20, message = "Soyadınız 2 ile 20 karakter arası olmalıdır.")
    private String lastName;

    @NotBlank(message = "Lütfen bir telefon numarası giriniz.")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Geçerli bir telefon numarası giriniz.")
    private String phoneNumber;

    private Gender gender;

    @Size(max = 255, message = "Adres 255 karakterden uzun olamaz.")
    private String address;

    public String getTckNo() {
        return tckNo;
    }

    public void setTckNo(String tckNo) {
        this.tckNo = tckNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


}