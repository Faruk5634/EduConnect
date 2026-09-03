import re
import sys

with open('frontend/src/pages/portals/StudentPanel.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace announcements section
announcements_pattern = re.compile(r'\{activeTab === \'announcements\' && \((.*?)\)\}', re.DOTALL)
ann_replacement = '''{activeTab === 'announcements' && (
                    <SharedAnnouncementModule
                        announcements={announcements}
                        userGrade={profile?.grade}
                    />
                )}'''
content = announcements_pattern.sub(ann_replacement, content)

# Replace messaging section
messages_pattern = re.compile(r'\{activeTab === \'messages\' && \((.*?)\)\}', re.DOTALL)
msg_replacement = '''{activeTab === 'messages' && (
                    <SharedMessagingModule
                        messages={messages}
                        onSendMessage={handleSendMessageDirect}
                        onReadMessage={handleReadMessageDirect}
                        userRoleLabel="Öğrenci Hesabı"
                    />
                )}'''
content = messages_pattern.sub(msg_replacement, content)

# Replace profile section
profile_pattern = re.compile(r'\{activeTab === \'profile\' && \((.*?)\)\}', re.DOTALL)
prof_replacement = '''{activeTab === 'profile' && (
                    <SharedProfileModule
                        headerInfo={{
                            initials: getInitials(profile?.firstName, profile?.lastName),
                            firstName: profile?.firstName || '',
                            lastName: profile?.lastName || '',
                            badgeText: profile?.grade ? ${profile.grade} SINIFI ÖĞRENCİSİ : undefined
                        }}
                        contactInfo={[
                            { label: 'Sistem Kullanıcı Adı', value: @, valueClass: 'text-indigo-600' },
                            { label: 'Telefon Numarası', value: profile?.phone || 'Belirtilmemiş' },
                            { label: 'E-Posta Adresi', value: profile?.email || 'Belirtilmemiş' }
                        ]}
                        additionalInfoTitle="Kayıt Bilgileri"
                        additionalInfo={[
                            { label: 'Okul Numarası', value: profile?.schoolNumber },
                            { label: 'Kayıtlı Veli', value: profile?.parentFullName || 'Belirtilmemiş' }
                        ]}
                        initialFormState={{
                            firstName: profile?.firstName || '',
                            lastName: profile?.lastName || '',
                            email: profile?.email || '',
                            phone: profile?.phone || ''
                        }}
                        onUpdateProfile={handleProfileUpdateDirect}
                        hideEditOptions={isParentViewing}
                    />
                )}'''
content = profile_pattern.sub(prof_replacement, content)

# Add imports for new modules
import_idx = content.find('import ')
new_imports = '''import SharedAnnouncementModule from '../../components/shared/SharedAnnouncementModule';
import SharedMessagingModule from '../../components/shared/SharedMessagingModule';
import SharedProfileModule from '../../components/shared/SharedProfileModule';
'''
content = content[:import_idx] + new_imports + content[import_idx:]

with open('frontend/src/pages/portals/StudentPanel.tsx', 'w', encoding='utf-8') as f:
    f.write(content)

print("Replaced sections in StudentPanel.tsx")
