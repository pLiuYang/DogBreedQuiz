# API Integration Test Guide

## Overview
This document provides testing instructions for the dog.ceo API integration in the Dog Breed Quiz App.

## API Endpoints Integrated

### 1. Get All Breeds
- **Endpoint**: `https://dog.ceo/api/breeds/list/all`
- **Method**: GET
- **Purpose**: Fetch all available dog breeds and their sub-breeds
- **Implementation**: `DogApiService.getAllBreeds()`

### 2. Get Random Breed Image
- **Endpoint**: `https://dog.ceo/api/breed/{breed}/images/random`
- **Method**: GET
- **Purpose**: Get a random image for a specific breed
- **Implementation**: `DogApiService.getRandomBreedImage()`

### 3. Get Random Sub-Breed Image
- **Endpoint**: `https://dog.ceo/api/breed/{breed}/{subBreed}/images/random`
- **Method**: GET
- **Purpose**: Get a random image for a specific sub-breed
- **Implementation**: `DogApiService.getRandomSubBreedImage()`

## Testing Instructions

### Manual API Testing
You can test the API endpoints directly using curl:

```bash
# Test breeds list
curl "https://dog.ceo/api/breeds/list/all"

# Test breed image
curl "https://dog.ceo/api/breed/retriever/images/random"

# Test sub-breed image
curl "https://dog.ceo/api/breed/retriever/golden/images/random"
```

### Expected API Responses

#### Breeds List Response
```json
{
  "message": {
    "retriever": ["chesapeake", "curly", "flatcoated", "golden"],
    "terrier": ["american", "australian", "bedlington", "border"],
    "hound": ["afghan", "basset", "blood", "english"]
  },
  "status": "success"
}
```

#### Random Image Response
```json
{
  "message": "https://images.dog.ceo/breeds/retriever-golden/n02099601_100.jpg",
  "status": "success"
}
```

## App Integration Features

### 1. Dynamic Breed Loading
- App loads breeds from dog.ceo API instead of static data
- Supports both main breeds and sub-breeds
- Caches data for 30 minutes to reduce API calls

### 2. Real Dog Images
- All quiz questions use real dog photos from the API
- Images are fetched dynamically for each breed
- Coil library handles image loading and caching

### 3. Error Handling
- Network error handling with user-friendly messages
- Fallback to static data if API is unavailable
- Retry functionality for failed requests

### 4. Loading States
- Loading indicators while fetching data
- Smooth transitions between loading and content states
- Progress feedback for better user experience

## Architecture Components

### API Layer
- `DogApiService`: Retrofit interface for API calls
- `DogApiRepository`: Repository handling API calls and caching
- `DogApiModels`: Data models for API responses

### Data Layer
- `DogBreedRepository`: Updated to use API data
- `DogBreedMapper`: Converts API data to internal models
- Caching strategy for improved performance

### Dependency Injection
- Hilt modules for network dependencies
- Singleton scoping for repositories and API services
- Proper dependency injection throughout the app

## Performance Optimizations

### 1. Caching Strategy
- In-memory caching for breeds (30-minute validity)
- Image caching handled by Coil library
- Reduced API calls for better performance

### 2. Data Limiting
- Limited to 50 breeds for optimal quiz performance
- Maximum 3 sub-breeds per main breed
- Balanced dataset for varied quiz questions

### 3. Error Recovery
- Graceful fallback to static data
- Retry mechanisms for failed requests
- User-friendly error messages

## Verification Checklist

- [ ] App loads breeds from dog.ceo API
- [ ] Quiz displays real dog images
- [ ] Loading states work correctly
- [ ] Error handling functions properly
- [ ] Offline mode with fallback data
- [ ] Caching reduces redundant API calls
- [ ] All existing UI features preserved
- [ ] Animations and transitions work smoothly
- [ ] Hilt dependency injection working
- [ ] Network permissions configured

## Known Limitations

1. **API Rate Limits**: The dog.ceo API may have rate limits
2. **Network Dependency**: Requires internet connection for full functionality
3. **Image Loading**: Some images may load slowly on poor connections
4. **Breed Coverage**: Limited to breeds available in dog.ceo API

## Future Enhancements

1. **Offline Storage**: Implement Room database for offline caching
2. **Image Preloading**: Preload images for better performance
3. **Progressive Loading**: Load breeds progressively as needed
4. **Custom Breed Data**: Allow users to add custom breed information