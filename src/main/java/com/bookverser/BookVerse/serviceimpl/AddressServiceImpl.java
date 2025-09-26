package com.bookverser.BookVerse.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.AddressRequestDto;
import com.bookverser.BookVerse.dto.AddressResponseDto;
import com.bookverser.BookVerse.entity.Address;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.AddressRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

	 private final AddressRepository addressRepository;
	    private final UserRepository userRepository;
	    private final ModelMapper modelMapper;

	    public AddressServiceImpl(AddressRepository addressRepository,
	                              UserRepository userRepository,
	                              ModelMapper modelMapper) {
	        this.addressRepository = addressRepository;
	        this.userRepository = userRepository;
	        this.modelMapper = modelMapper;
	    }
	@Override
	public AddressResponseDto addAddress(String userEmail, AddressRequestDto dto) {
		 User user = userRepository.findByEmail(userEmail)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        Address address = new Address();
	        address.setCity(dto.getCity());
	        address.setState(dto.getState());
	        address.setCountry(dto.getCountry());
	        address.setUser(user);

	        Address saved = addressRepository.save(address);
	        return modelMapper.map(saved, AddressResponseDto.class);
	}

	@Override
	public AddressResponseDto updateAddress(String userEmail, Long addressId, AddressRequestDto dto) {
		User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());

        return modelMapper.map(addressRepository.save(address), AddressResponseDto.class);
	}

	@Override
	public void deleteAddress(String userEmail, Long addressId) {
		User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressRepository.delete(address);
		
	}

	@Override
	public List<AddressResponseDto> getAddresses(String userEmail) {
		 User user = userRepository.findByEmail(userEmail)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        return addressRepository.findByUser(user)
	                .stream()
	                .map(address -> modelMapper.map(address, AddressResponseDto.class))
	                .collect(Collectors.toList());
	    
	}

}
