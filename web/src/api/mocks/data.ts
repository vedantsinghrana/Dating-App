// Seed data for the MSW mock server. Shapes/example values are taken
// straight from API_CONTRACT.md so the mock layer never drifts from the
// documented contract.

import type {
  DiscoverResult,
  Match,
  Message,
  Profile,
  Prompt,
} from '../types';

export const MOCK_USER_ID = '11111111-1111-1111-1111-111111111111';

const PROMPT_BANK: [string, string][] = [
  ['A perfect Sunday looks like', 'Farmers market, then doing absolutely nothing'],
  ['I will win an argument about', 'Whether a hot dog is a sandwich'],
  ['My most controversial opinion is', 'Pineapple belongs on pizza'],
  ['Two truths and a lie', "I've run a marathon, I hate coffee, I own 4 cacti"],
  ['Best travel story', 'Got lost in Lisbon and found the best bakery in the city'],
];

function promptsFor(seed: number): Prompt[] {
  return [0, 1, 2].map((i) => {
    const [question, answer] = PROMPT_BANK[(seed + i) % PROMPT_BANK.length];
    return { id: `prompt-${seed}-${i}`, question, answer };
  });
}

export const mockProfile: Profile = {
  userId: MOCK_USER_ID,
  name: 'Alex',
  birthdate: '1998-04-12',
  bio: 'Exploring the city one coffee shop at a time.',
  photos: [
    'https://picsum.photos/seed/self-1/600/800',
    'https://picsum.photos/seed/self-2/600/800',
  ],
  prompts: promptsFor(0),
  location: { lat: 12.97, lng: 77.59 },
  searchRadiusKm: 25,
};

const DISCOVER_NAMES = [
  'Priya',
  'Jordan',
  'Sam',
  'Maya',
  'Noah',
  'Riya',
  'Leo',
  'Zara',
];

export const mockDiscoverResults: DiscoverResult[] = DISCOVER_NAMES.map((name, i) => ({
  userId: `discover-${i}`,
  name,
  age: 24 + (i % 8),
  photos: [
    `https://picsum.photos/seed/${name}-1/600/800`,
    `https://picsum.photos/seed/${name}-2/600/800`,
  ],
  prompts: promptsFor(i + 1),
  distanceKm: Math.round((1 + i * 1.7) * 10) / 10,
}));

export const mockMatches: Match[] = [
  {
    matchId: 'match-1',
    otherUser: {
      userId: 'discover-0',
      name: 'Priya',
      photos: ['https://picsum.photos/seed/Priya-1/600/800'],
    },
    createdAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() + 45 * 60 * 60 * 1000).toISOString(),
    openingMoveDone: true,
  },
  {
    matchId: 'match-2',
    otherUser: {
      userId: 'discover-2',
      name: 'Sam',
      photos: ['https://picsum.photos/seed/Sam-1/600/800'],
    },
    createdAt: new Date(Date.now() - 40 * 60 * 60 * 1000).toISOString(),
    expiresAt: new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString(),
    openingMoveDone: false,
  },
];

export const mockMessagesByMatch: Record<string, Message[]> = {
  'match-1': [
    {
      id: 'msg-1',
      senderId: 'discover-0',
      content: "Team hot dog IS a sandwich, fight me 😄",
      sentAt: new Date(Date.now() - 2.5 * 60 * 60 * 1000).toISOString(),
    },
    {
      id: 'msg-2',
      senderId: MOCK_USER_ID,
      content: "Absolutely not, this is why we can't be friends",
      sentAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    },
  ],
  'match-2': [],
};
