# Samjhadoo - Comprehensive Project Specification

## 🚀 Core Updates & Additions

### 1. Voice Command Layer (Button-Press Speak)
- **Mentee Voice Commands**:
  - "Find me a mentor for [skill]."
  - "Book a session with [mentor name] tomorrow evening."
  - "Reschedule my session to [day/time]."
  - "Summarize my last session."
  - "I'm feeling low, connect me to friendly talk."

- **Mentor Voice Commands**:
  - "Mark me available for the next 2 hours."
  - "Add a note: [custom note]."
  - "Show my earnings this week."
  - "List my upcoming sessions."

### 2. Community-First Module
- **Community Tagging**:
  - Options: Student, Employee/Professional, Farmer, Woman/Girl, Senior Citizen
  - Verification process for discounts
  
- **Pricing Structure**:
  - Base unit: 10 minutes
  - Geo-based pricing (India: ₹49/10 min, US: $1.49/10 min, UK: £1.29/10 min)
  - Community discounts:
    - Students: 30-40% off
    - Women/Girls: 20-30% off
    - Farmers: 40-50% off
    - Senior Citizens: 20-30% off

### 3. Smart Search & Discovery
- **Search Features**:
  - Recent searches (last 5-10 queries)
  - Trending searches with badges (🔥 "Trending", 🆕 "New")
  - AI-powered autocomplete
  - Visual query integration

## 🛠 Technical Implementation

### Backend Updates
1. **New API Endpoints**:
   - `/api/voice/process` - Handle voice command processing
   - `/api/community/verify` - Community verification
   - `/api/pricing/calculate` - Dynamic pricing calculation

2. **Database Schema Additions**:
   ```sql
   CREATE TABLE community_members (
       id SERIAL PRIMARY KEY,
       user_id UUID REFERENCES users(id),
       community_type VARCHAR(50) NOT NULL,
       verification_status VARCHAR(20) DEFAULT 'PENDING',
       verification_data JSONB,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

### Frontend Updates
1. **New Components**:
   - `VoiceCommandButton.vue` - Voice input component
   - `CommunityVerificationModal.vue` - Verification flow
   - `SmartSearchBar.vue` - Enhanced search with suggestions

2. **State Management**:
   - Add voice command state
   - Track community verification status
   - Cache search history and suggestions

## 📅 Next Steps

1. **Immediate Tasks**:
   - [ ] Implement voice command processing service
   - [ ] Create community verification workflow
   - [ ] Update pricing calculation logic
   - [ ] Design and implement smart search UI

2. **Testing**:
   - [ ] Voice command accuracy testing
   - [ ] Community verification flow testing
   - [ ] Pricing calculation edge cases
   - [ ] Search performance optimization

3. **Deployment**:
   - [ ] Staging environment setup
   - [ ] Performance monitoring
   - [ ] A/B testing for new features

## 📝 Notes
- Ensure all voice data processing complies with privacy policies
- Implement proper error handling for voice recognition failures
- Add analytics to track feature adoption and success metrics
- Consider adding multi-language support for voice commands
