const fs = require('fs');
let content = fs.readFileSync('frontend/src/pages/portals/StudentPanel.tsx', 'utf-8');

// 1. Replace the JSX for announcements, messages, and profile
content = content.replace(/\{activeTab === 'announcements' && \([\s\S]*?(?=\{activeTab === 'messages')/, `{activeTab === 'announcements' && (
                        <SharedAnnouncementModule
                            announcements={announcements}
                            userGrade={profile?.grade}
                        />
                    )}
                    
                    `);

content = content.replace(/\{activeTab === 'messages' && \([\s\S]*?(?=\{activeTab === 'profile')/, `{activeTab === 'messages' && (
                        <SharedMessagingModule
                            messages={messages}
                            onSendMessage={handleSendMessage}
                            onReadMessage={handleReadMessage}
                            userRoleLabel="Öğrenci Hesabı"
                        />
                    )}
                    
                    `);

content = content.replace(/\{activeTab === 'profile' && \([\s\S]*?(?=\}\n\s*<\/div>\n\s*<\/main>)/, `{activeTab === 'profile' && (
                        <SharedProfileModule
                            headerInfo={{
                                initials: getInitials(profile?.firstName, profile?.lastName),
                                firstName: profile?.firstName || '',
                                lastName: profile?.lastName || '',
                                badgeText: profile?.grade ? \`\${profile.grade} SINIFI ÖĞRENCİSİ\` : undefined
                            }}
                            contactInfo={[
                                { label: 'Sistem Kullanıcı Adı', value: \`@\${profile?.username}\`, valueClass: 'text-indigo-600' },
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
                            onUpdateProfile={handleProfileUpdate}
                            hideEditOptions={isParentViewing}
                        />
                    )}`);

// Add imports
const imports = `import SharedAnnouncementModule from '../../components/shared/SharedAnnouncementModule';
import SharedMessagingModule from '../../components/shared/SharedMessagingModule';
import SharedProfileModule from '../../components/shared/SharedProfileModule';
`;
if (!content.includes('SharedAnnouncementModule')) {
    content = content.replace(/import React/, imports + 'import React');
}

// 2. Refactor handleSendMessage
content = content.replace(
    /const handleSendMessage = async \(e: React\.FormEvent\) => \{[\s\S]*?catch \{[\s\S]*?\}\n    \};/,
    `const handleSendMessage = async (receiverId: string, subject: string, content: string) => {
        if (!receiverId) {
            showToast('Lütfen listeden bir alıcı seçin.', 'error');
            return;
        }
        try {
            await sendMessage({
                receiverId,
                subject,
                content
            });
            showToast('Mesaj başarıyla gönderildi!', 'success');
            await fetchInitialData();
        } catch {
            showToast('Mesaj gönderilemedi.', 'error');
        }
    };`
);

// 3. Refactor handleProfileUpdate
content = content.replace(
    /const handleProfileUpdate = async \(e: React\.FormEvent\) => \{[\s\S]*?catch \{[\s\S]*?\}\n    \};/,
    `const handleProfileUpdate = async (viewMode: string, formData: any) => {
        if (isParentViewing) {
            showToast('Öğrenci bilgileri veli yetkisiyle güncellenemez.', 'error');
            return;
        }
        try {
            if (viewMode === 'editPassword') {
                if (!formData.currentPassword || !formData.newPassword) {
                    showToast('Lütfen mevcut ve yeni şifrenizi girin.', 'error');
                    return;
                }
                await api.put('/users/me', {
                    password: formData.newPassword,
                    currentPassword: formData.currentPassword
                });
            } else {
                await api.put('/users/me', {
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    email: formData.email,
                    phone: formData.phone
                });
            }
            showToast('Profil bilgileriniz başarıyla güncellendi.', 'success');
            await fetchInitialData();
        } catch {
            showToast('Profil güncellenemedi.', 'error');
        }
    };`
);

// Remove unused states
content = content.replace(/const \[selectedAnnouncement.*?\n/, '');
content = content.replace(/const \[announcementSearch.*?\n/, '');
content = content.replace(/const \[announcementTypeFilter.*?\n/, '');
content = content.replace(/const \[announcementSort.*?\n/, '');
content = content.replace(/const \[mailBoxView.*?\n/, '');
content = content.replace(/const \[rightPaneMode.*?\n/, '');
content = content.replace(/const \[msgReceiverId.*?\n/, '');
content = content.replace(/const \[msgSubject.*?\n/, '');
content = content.replace(/const \[msgContent.*?\n/, '');
content = content.replace(/const \[selectedMessage.*?\n/, '');
content = content.replace(/const \[userSearchQuery.*?\n/, '');
content = content.replace(/const \{ results: searchResults.*?\n/, '');
content = content.replace(/const \[showSearchDropdown.*?\n/, '');
content = content.replace(/const \[selectedReceiverName.*?\n/, '');
content = content.replace(/const \[profileViewMode.*?\n/, '');
content = content.replace(/const \[updateForm.*?\n[\s\S]*?\}\);\n/, '');

fs.writeFileSync('frontend/src/pages/portals/StudentPanel.tsx', content, 'utf-8');
console.log("StudentPanel refactored");
